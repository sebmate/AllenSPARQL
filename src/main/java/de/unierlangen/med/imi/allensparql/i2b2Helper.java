/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.unierlangen.med.imi.allensparql;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.i2b2.xsd.cell.crc.psm.querydefinition._1.ItemType;
import org.i2b2.xsd.cell.crc.psm.querydefinition._1.ItemType.ConstrainByModifier;
import org.i2b2.xsd.cell.crc.psm.querydefinition._1.PanelType;
import org.i2b2.xsd.cell.crc.psm.querydefinition._1.QueryDefinitionType;

/**
 *
 * @author matesn
 */
public class i2b2Helper {

    private int patientLimit;
    private String availableVariables = "";

    private List i2b2Variables = new ArrayList();
    private List sqlStatements = new ArrayList();

    public void processI2b2Query(String xml) {

        String sqlStatement = "";
        setAvailableVariables("");

        try {
            JAXBContext jaxbContext;
            jaxbContext = JAXBContext
                    .newInstance("org.i2b2.xsd.cell.crc.psm.querydefinition._1");

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            @SuppressWarnings("unchecked")
            JAXBElement<QueryDefinitionType> queryDefinition;
            queryDefinition = (JAXBElement<QueryDefinitionType>) unmarshaller
                    .unmarshal(new StringReader(xml));

            QueryDefinitionType definition = queryDefinition.getValue();

            String queryName = definition.getQueryName();

            String PatientSQL = "SELECT PSC.PATIENT_NUM PATIENT_NUM FROM QT_QUERY_MASTER QM, QT_QUERY_INSTANCE QI, QT_QUERY_RESULT_INSTANCE QRI, QT_PATIENT_SET_COLLECTION PSC WHERE QM.QUERY_MASTER_ID = QI.QUERY_MASTER_ID AND QI.QUERY_INSTANCE_ID = QRI.QUERY_INSTANCE_ID AND QRI.RESULT_INSTANCE_ID = PSC.RESULT_INSTANCE_ID AND QM.NAME = '" + queryName + "'";

            if (patientLimit > 0) {
                PatientSQL += " AND ROWNUM <= " + patientLimit;
            }

            String optiQuery = " AND PATIENT_NUM IN (" + PatientSQL + ")";

            List<PanelType> panelList = definition.getPanel();

            for (int panel = 0; panel < panelList.size(); panel++) {

                System.out.print("Panel " + panel);
                if (panelList.get(panel).getInvert() == 1) {
                    System.out.print(" (exclude)");
                }
                System.out.println();

                if (panelList.get(panel).getInvert() == 1) {
                    System.out.print(" (exclude)");
                }

                List<ItemType> itemList = panelList.get(panel).getItem();
                for (int item = 0; item < itemList.size(); item++) {
                    String itemKey = itemList.get(item).getItemKey().toString()
                            .replace("\\\\i2b2\\", "\\");
                    String itemIcon = itemList.get(item).getItemIcon();
                    String itemName = itemList.get(item).getItemName();
                    String simplifiedName = simplify(itemName);
                    availableVariables += simplifiedName + ", ";
                    getI2b2Variables().add(simplifiedName);
                    String sqlQuery = "";
                    ConstrainByModifier modifier = itemList.get(item)
                            .getConstrainByModifier();

                    if (modifier == null) {
                        if (itemIcon != null) {
                            if (itemIcon.equals("LA")) {
                                System.out.println("    Item: " + itemKey);
                                //sqlQuery = "SELECT PATIENT_NUM, (CASE WHEN NVAL_NUM IS NOT NULL THEN NVAL_NUM ELSE 0 END) VALUE, START_DATE, END_DATE FROM OBSERVATION_FACT WHERE CONCEPT_CD IN (SELECT CONCEPT_CD FROM CONCEPT_DIMENSION WHERE CONCEPT_PATH = '"
                                sqlQuery = "SELECT * FROM OBSERVATION_FACT WHERE CONCEPT_CD IN (SELECT CONCEPT_CD FROM CONCEPT_DIMENSION WHERE CONCEPT_PATH = '"
                                        + itemKey + "')" + optiQuery;
                            } else {
                                System.out.println("    Item: " + itemKey + "%");

                                //sqlQuery = "SELECT PATIENT_NUM, (CASE WHEN NVAL_NUM IS NOT NULL THEN NVAL_NUM ELSE 0 END) VALUE, START_DATE, END_DATE FROM OBSERVATION_FACT WHERE CONCEPT_CD IN (SELECT CONCEPT_CD FROM CONCEPT_DIMENSION WHERE CONCEPT_PATH LIKE '"
                                sqlQuery = "SELECT * FROM OBSERVATION_FACT WHERE CONCEPT_CD IN (SELECT CONCEPT_CD FROM CONCEPT_DIMENSION WHERE CONCEPT_PATH LIKE '"
                                        + itemKey + "%')" + optiQuery;
                            }
                        }
                    } else {
                        String modifierKey = modifier.getModifierKey().replace(
                                "\\\\i2b2\\", "\\");
                        String modifierConcept = modifier.getModifierName();
                        if (itemIcon.equals("LA")) {
                            System.out.println("    Item: " + itemKey + " | "
                                    + modifierKey);
                            //sqlQuery = "SELECT PATIENT_NUM, (CASE WHEN NVAL_NUM IS NOT NULL THEN NVAL_NUM ELSE 0 END) VALUE, START_DATE, END_DATE FROM OBSERVATION_FACT WHERE CONCEPT_CD IN (SELECT CONCEPT_CD FROM CONCEPT_DIMENSION WHERE CONCEPT_PATH = '"
                            sqlQuery = "SELECT * FROM OBSERVATION_FACT WHERE CONCEPT_CD IN (SELECT CONCEPT_CD FROM CONCEPT_DIMENSION WHERE CONCEPT_PATH = '"
                                    + itemKey
                                    + "') AND MODIFIER_CD IN ( SELECT MODIFIER_CD FROM MODIFIER_DIMENSION WHERE MODIFIER_PATH = '"
                                    + modifierKey + "')" + optiQuery;
                        } else {
                            System.out.println("    Item: " + itemKey + "% | "
                                    + modifierKey);
                            //sqlQuery = "SELECT PATIENT_NUM, (CASE WHEN NVAL_NUM IS NOT NULL THEN NVAL_NUM ELSE 0 END) VALUE, START_DATE, END_DATE FROM OBSERVATION_FACT WHERE CONCEPT_CD IN (SELECT CONCEPT_CD FROM CONCEPT_DIMENSION WHERE CONCEPT_PATH LIKE '"
                            sqlQuery = "SELECT * FROM OBSERVATION_FACT WHERE CONCEPT_CD IN (SELECT CONCEPT_CD FROM CONCEPT_DIMENSION WHERE CONCEPT_PATH LIKE '"
                                    + itemKey
                                    + "%') AND MODIFIER_CD IN ( SELECT MODIFIER_CD FROM MODIFIER_DIMENSION WHERE MODIFIER_PATH = '"
                                    + modifierKey + "')" + optiQuery;
                        }
                    }

                    sqlStatement = sqlQuery;
                    getSqlStatements().add(sqlStatement);
                }
            }

            writeVariablesToFile();
            // System.out.println(sqlStatement);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private String simplify(String input) {

        // Taken partially from: http://gordon.koefner.at/blog/coding/replacing-german-umlauts/
        String simplified = "";

        //replace all lower Umlauts
        simplified
                = input
                .replaceAll("ü", "ue")
                .replaceAll("ö", "oe")
                .replaceAll("ä", "ae")
                .replaceAll("ß", "ss");

        //first replace all capital umlaute in a non-capitalized context (e.g. Übung)
        simplified
                = simplified
                .replaceAll("Ü(?=[a-zäöüß ])", "Ue")
                .replaceAll("Ö(?=[a-zäöüß ])", "Oe")
                .replaceAll("Ä(?=[a-zäöüß ])", "Ae");

        //now replace all the other capital umlaute
        simplified
                = simplified
                .replaceAll("Ü", "UE")
                .replaceAll("Ö", "OE")
                .replaceAll("Ä", "AE");

        simplified
                = simplified
                .replaceAll("[^A-Za-z0-9]", "_");

        return simplified;
    }

    void setPatientLimit(int patientLimit) {
        this.patientLimit = patientLimit;
    }

    /**
     * @return the availableVariables
     */
    public String getAvailableVariables() {
        if (availableVariables.length() >= 2) {
            return availableVariables.substring(0, availableVariables.length() - 2);
        } else {
            return "";
        }
    }

    public String getAvailableVariables2() {
        String vars = "";
        for (int a = 0; a < getI2b2Variables().size(); a++) {
            vars += getI2b2Variables().get(a) + ", ";
        }
        vars = vars.substring(0, vars.length() - 2);
        return vars;
    }

    /**
     * @param availableVariables the availableVariables to set
     */
    public void setAvailableVariables(String availableVariables) {
        this.availableVariables = availableVariables;
    }

    void writeVariablesToFile() {
        try {
            PrintWriter writer = new PrintWriter("MedicalConcepts.txt", "UTF-8");
            for (int a = 0; a < getI2b2Variables().size(); a++) {
                writer.println(getI2b2Variables().get(a));
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the i2b2Variables
     */
    public List getI2b2Variables() {
        return i2b2Variables;
    }

    /**
     * @param i2b2Variables the i2b2Variables to set
     */
    public void setI2b2Variables(List i2b2Variables) {
        this.i2b2Variables = i2b2Variables;
    }

    /**
     * @return the sqlStatements
     */
    public List getSqlStatements() {
        return sqlStatements;
    }

    /**
     * @param sqlStatements the sqlStatements to set
     */
    public void setSqlStatements(List sqlStatements) {
        this.sqlStatements = sqlStatements;
    }

}
