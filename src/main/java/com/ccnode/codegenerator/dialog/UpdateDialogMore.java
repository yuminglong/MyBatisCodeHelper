package com.ccnode.codegenerator.dialog;

import com.ccnode.codegenerator.dialog.datatype.ClassFieldInfo;
import com.ccnode.codegenerator.dialog.dto.MapperDto;
import com.ccnode.codegenerator.dialog.dto.mybatis.MapperMethod;
import com.ccnode.codegenerator.dialog.dto.mybatis.MapperMethodEnum;
import com.ccnode.codegenerator.dialog.dto.mybatis.MapperSql;
import com.ccnode.codegenerator.dialog.dto.mybatis.ResultMap;
import com.ccnode.codegenerator.util.PsiClassUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by bruce.ge on 2016/12/27.
 */
public class UpdateDialogMore extends DialogWrapper {
    private Project myProject;

    private PsiClass myClass;

    private XmlFile myXmlFile;

    private List<ClassFieldInfo> newAddFields;

    private List<String> deletedFields;

    private PsiClass myDaoClass;

    private List<ClassFieldInfo> propFields;

    private List<String> existingFields;

    private String message;

    private MapperDto mapperDto;

    public UpdateDialogMore(Project project, PsiClass srcClass, XmlFile xmlFile, PsiClass nameSpaceDaoClass) {
        super(project, true);
        this.myProject = project;
        this.myClass = srcClass;
        this.myXmlFile = xmlFile;
        this.myDaoClass = nameSpaceDaoClass;
        initNeedUpdate();
        setTitle("update mapper xml");

        init();
    }

    private void initNeedUpdate() {
        this.propFields = PsiClassUtil.buildPropFieldInfo(myClass);
        mapperDto = parseXml();
        //get the new added filed and type.
        extractAddAndDelete();
        if (newAddFields.size() == 0 && deletedFields.size() == 0) {
            message = "there is no field to update or add, please check again with your resultMap";
            return;
        }

    }

    private void extractAddAndDelete() {
        newAddFields = new ArrayList<>();
        deletedFields = new ArrayList<>();
        Set<String> allFieldMap = new HashSet<>();
        propFields.forEach((item) -> {
            allFieldMap.add(item.getFieldName().toLowerCase());
        });
        Set<String> existingMap = new HashSet<>();

        existingFields.forEach((item) -> existingMap.add(item.toLowerCase()));

        propFields.forEach((item) -> {
            if (!existingMap.contains(item.getFieldName().toLowerCase())) {
                newAddFields.add(item);
            }
        });

        existingFields.forEach((item) -> {
            if (!allFieldMap.contains(item.toLowerCase())) {
                deletedFields.add(item);
            }
        });
    }

    private MapperDto parseXml() {
        XmlTag[] subTags = myXmlFile.getRootTag().getSubTags();
        MapperDto dto = new MapperDto();
        List<ResultMap> resultMaps = new ArrayList<>();
        List<MapperSql> sqls = new ArrayList<>();
        Map<String, MapperMethod> methodMap = new HashMap<>();
        for (XmlTag subTag : subTags) {
            String name = subTag.getName();
            switch (name) {
                case "resultMap": {
                    ResultMap resultMap = buildResultMap(subTag);
                    if (resultMap.getType().equals(myClass.getQualifiedName())) {
                        existingFields = extractFileds(subTag);
                    }
                    resultMaps.add(resultMap);
                    break;
                }
                case "sql": {
                    MapperSql sql = buildSql(subTag);
                    sqls.add(sql);
                    break;
                }
                case "insert": {
                    MapperMethod s = extractMethod(subTag, MapperMethodEnum.INSERT);
                    String id = subTag.getAttributeValue("id");
                    methodMap.put(id, s);
                    break;
                }
                case "update": {
                    MapperMethod s = extractMethod(subTag, MapperMethodEnum.UPDATE);
                    String id = subTag.getAttributeValue("id");
                    methodMap.put(id, s);
                    break;
                }
                case "delete": {
                    MapperMethod s = extractMethod(subTag, MapperMethodEnum.DELETE);
                    String id = subTag.getAttributeValue("id");
                    methodMap.put(id, s);
                    break;
                }
                case "select": {
                    MapperMethod s = extractMethod(subTag, MapperMethodEnum.SELECT);
                    String id = subTag.getAttributeValue("id");
                    methodMap.put(id, s);
                    break;
                }
            }

        }

        dto.setResultMapList(resultMaps);
        dto.setMapperMethodMap(methodMap);
        dto.setSqls(sqls);
        return dto;
    }

    private MapperMethod extractMethod(XmlTag subTag, MapperMethodEnum insert) {
        MapperMethod mapperMethod = new MapperMethod();
        mapperMethod.setType(insert);
        mapperMethod.setXmlTag(subTag);
        return mapperMethod;
    }

    private MapperSql buildSql(XmlTag subTag) {
        String id = subTag.getAttributeValue("id");
        MapperSql sql = new MapperSql();
        sql.setId(id);
        sql.setTag(subTag);
        return sql;
    }

    private List<String> extractFileds(XmlTag subTag) {
        List<String> props = new ArrayList<>();
        for (XmlTag tag : subTag.getSubTags()) {
            String property = tag.getAttributeValue("property");
            if (StringUtils.isNotBlank(property)) {
                props.add(property.trim());
            }
        }
        return props;
    }

    private ResultMap buildResultMap(XmlTag subTag) {
        ResultMap resultMap = new ResultMap();
        String id = subTag.getAttributeValue("id");
        String type = subTag.getAttributeValue("type");
        resultMap.setId(id);
        resultMap.setType(type);
        resultMap.setTag(subTag);
        return resultMap;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridBagLayout());
        GridBagConstraints bag = new GridBagConstraints();


        return jPanel;
    }
}