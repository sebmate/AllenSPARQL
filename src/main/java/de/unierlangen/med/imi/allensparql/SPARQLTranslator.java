/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.unierlangen.med.imi.allensparql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author matesn
 */
class SPARQLTranslator {

    String Allen = "";

    ArrayList<AllenStatement> allenStatements = new ArrayList<AllenStatement>();
    private String relativeTemporalCriteria;

    SPARQLTranslator(String Allen) {
        String[] AllenStatementsInput = Allen.split(";");
        for (int a = 0; a < AllenStatementsInput.length; a++) {
            System.out.println(AllenStatementsInput[a]);
            AllenStatement as = new AllenStatement(AllenStatementsInput[a]);
            allenStatements.add(as);
        }
    }

    String getQueryPart(IntervalDescription interval1, String relation, IntervalDescription interval2) {

        relation = relation.replaceAll(",_", "|a:");

        String result = "";
        String conceptIdentifier1 = interval1.getIntervallNameSPARQL();
        String conceptIdentifier2 = interval2.getIntervallNameSPARQL();
        String IntervalPrefix = "int_";

        if (!interval1.isNegation() && !interval2.isNegation()) {
            result += "  # ===============[ " + interval1.getDescription() + " " + relation.replaceAll("a:", "") + " " + interval2.getDescription() + " ]=============== \n\n";

            if (!interval1.isDuration() && !interval2.isDuration()) {
                result += "  ?" + IntervalPrefix + conceptIdentifier1 + " a:" + relation + " ?" + IntervalPrefix + conceptIdentifier2 + " . \n";
            } else {
                result += "  # Note: No Allen statement is added, because at least\n";
                result += "  #       one of the intervals is a duration interval.\n";
            }
        }

        if ((interval1.isNegation() || interval2.isNegation()) && !(interval1.isNegation() && interval2.isNegation())) {
            // Describe interval 1:
            result += "  FILTER NOT EXISTS {?" + IntervalPrefix + conceptIdentifier1 + " a:" + relation + " ?" + IntervalPrefix + conceptIdentifier2 + "} . \n";
        }

        // Describe interval 1:
        if (!interval1.isDuration()) {
            result += "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasConcept \"" + interval1.getItemType() + "\" .\n"
                    + "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasPatient ?result .\n";
            result += "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasValue ?value_of_" + IntervalPrefix + conceptIdentifier1 + " .\n";
            result += "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasStartDateUnix ?start_of_" + IntervalPrefix + conceptIdentifier1 + " .\n";
            result += "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasEndDateUnix ?end_of_" + IntervalPrefix + conceptIdentifier1 + " .\n";
        }

        // Describe interval 2:
        if (!interval2.isDuration()) {
            result += "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasConcept \"" + interval2.getItemType() + "\" .\n"
                    + "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasPatient ?result .\n";
            result += "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasValue ?value_of_" + IntervalPrefix + conceptIdentifier2 + " .\n";
            result += "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasStartDateUnix ?start_of_" + IntervalPrefix + conceptIdentifier2 + " .\n";
            result += "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasEndDateUnix ?end_of_" + IntervalPrefix + conceptIdentifier2 + " .\n";
        }

        result += "\n\n";

        // Value filtering:
        if ((!interval1.getValueModifier().equals(""))) {
            String compValue = "";
            if (isNumeric(interval1.getValue(interval1.getValueModifier()))) {
                compValue = interval1.getValueModifier();
            } else {
                compValue = interval1.getComparator(interval1.getValueModifier()) + "?value_of_" + IntervalPrefix + makeSPARQL(interval1.getValue(interval1.getValueModifier()));
            }
            result += "  FILTER (" + " ?value_of_" + IntervalPrefix + conceptIdentifier1 + " " + compValue + ") .\n\n";
        }

        if ((!interval2.getValueModifier().equals(""))) {
            String compValue = "";
            if (isNumeric(interval2.getValue(interval2.getValueModifier()))) {
                compValue = interval2.getValueModifier();
            } else {
                compValue = interval2.getComparator(interval2.getValueModifier()) + " ?value_of_" + IntervalPrefix + makeSPARQL(interval2.getValue(interval2.getValueModifier()));
            }
            result += "  FILTER (" + "?value_of_" + IntervalPrefix + conceptIdentifier2 + " " + compValue + ") .\n\n";
        }

        // Subinterval counting:
        if ((!interval1.getOccuranceModifier().equals(""))) {
            result += "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasSubCount ?count_of_" + IntervalPrefix + conceptIdentifier1 + " .\n";
            result += "  FILTER (" + "?count_of_" + IntervalPrefix + conceptIdentifier1 + " " + interval1.getOccuranceModifier() + ") .\n\n";
        }
        if ((!interval2.getOccuranceModifier().equals(""))) {
            result += "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasSubCount ?count_of_" + IntervalPrefix + conceptIdentifier2 + " .\n";
            result += "  FILTER (" + "?count_of_" + IntervalPrefix + conceptIdentifier2 + " " + interval2.getOccuranceModifier() + ") .\n\n";
        }

        // Constrain by duration interval:
        if ((interval2.isDuration())) {
            String comp = "";
            if (relation.equals("finished by") || relation.equals("contains") || relation.equals("started by")) {
                comp = ">";
            }
            if (relation.equals("finishes") || relation.equals("during") || relation.equals("starts")) {
                comp = "<";
            }
            if (relation.equals("equals")) {
                comp = "=";
            }
            if (comp.equals("")) {
                //result += "  # ERROR: A duration can not be used for constraining another \n"
                //        + "  #        interval over the \"" + relation.replaceAll("a:", "") + "\" relation. This is only\n";
                //result += "  #        possible with the relations \"finishes\", \"during\",\n"
                //        + "  #        \"starts\", \"equals\" and their reverses.\n\n";
            } else {
                result += "  # Note: Limiting the length of interval \"" + conceptIdentifier1 + "\" to " + comp + " " + interval2.getIntervalName() + ":\n";
                result += "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasStartDateUnix ?start_of_" + IntervalPrefix + conceptIdentifier1 + " .\n";
                result += "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasEndDateUnix ?end_of_" + IntervalPrefix + conceptIdentifier1 + " .\n";
                result += "  FILTER (?end_of_" + IntervalPrefix + conceptIdentifier1 + " - ?start_of_" + IntervalPrefix + conceptIdentifier1 + " " + comp + " " + interval2.getDurationSeconds() + ") .\n\n";
            }
        }

        if ((interval1.isDuration())) {
            String comp = "";
            if (relation.equals("finished by") || relation.equals("contains") || relation.equals("started by")) {
                comp = "<";
            }
            if (relation.equals("finishes") || relation.equals("during") || relation.equals("starts")) {
                comp = ">";
            }
            if (relation.equals("equals")) {
                comp = "=";
            }
            if (comp.equals("")) {
                //result += "  # ERROR: A duration can not be used for constraining another \n"
                //        + "  #        interval over the \"" + relation.replaceAll("a:", "") + "\" relation. This is only\n";
                //result += "  #        possible with the relations \"finishes\", \"during\",\n"
                //        + "  #        \"starts\", \"equals\" and their reverses.\n\n";
            } else {
                result += "  # Note: Limiting the length of interval \"" + conceptIdentifier2 + "\" to " + comp + " " + interval1.getIntervalName() + ":\n";
                result += "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasStartDateUnix ?start_of_" + IntervalPrefix + conceptIdentifier2 + " .\n";
                result += "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasEndDateUnix ?end_of_" + IntervalPrefix + conceptIdentifier2 + " .\n";
                result += "  FILTER (?end_of_" + IntervalPrefix + conceptIdentifier2 + " - ?start_of_" + IntervalPrefix + conceptIdentifier2 + " " + comp + " " + interval1.getDurationSeconds() + ") .\n\n";
            }
        }

        return result;
    }

    String translate() {

        if (allenStatements.size() == 0) {
            return "";
        }

        String result
                = //"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                //+ "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
                "PREFIX a: <https://www.imi.med.fau.de/AllenSparql/>\n\n"
                // + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n\n"
                + "SELECT DISTINCT ?result\n"
                + "WHERE {\n\n";

        boolean OK = false;

        String knownIntervals = "";

        for (int a = 0; a < allenStatements.size(); a++) {
            AllenStatement as = allenStatements.get(a);

            //if (!as.getRelation().contains(",")) {        // Exclude "possible" Allen relations
            OK = true;

            String iName1 = as.getInterval1().getIntervalName();
            String iName2 = as.getInterval2().getIntervalName();

            /*if (as.getInterval1().isDuration() && knownIntervals.contains(iName1)) {
                result += "  # WARNING: The duration interval \"" + iName1 + "\" is used more than once in the query graph. \n";
                result += "  #          Note that modelling relative duration constraints is not supported.\n\n";
            }
            if (as.getInterval2().isDuration() && knownIntervals.contains(iName2)) {
                result += "  # WARNING: The duration interval \"" + iName2 + "\" is used more than once in the query graph. \n";
                result += "  #          Note that modelling relative duration constraints is not supported.\n\n";
            }*/
            knownIntervals += iName1 + ";";
            knownIntervals += iName2 + ";";

            result += getQueryPart(as.getInterval1(), as.getRelation(), as.getInterval2());
            //}
        }

        if (!relativeTemporalCriteria.equals("")) {
            result += "  # ===============[ Constraints on relative intervals (derived with SageMath) ]=============== \n\n";
            result += relativeTemporalCriteria + "\n\n";
        }
        result += "} ORDER BY ?result";

        result = cleanUpSPARQL(result);
        result = optimizeSPARQL(result);

        if (!OK) {
            return "";
        }

        return result;
    }

    private String cleanUpSPARQL(String code) { // remove duplicate SPARQL filter criteria
        String[] lines = code.split("\n");
        Hashtable htable = new Hashtable();
        String result = "";
        for (int a = 0; a < lines.length; a++) {
            if (!htable.contains(lines[a]) || lines[a].trim().equals("") || lines[a].trim().startsWith("#")) {
                result += lines[a] + "\n";
                htable.put(a, lines[a]);
            }
        }
        return result.replaceAll("\n\n\n+", "\n\n");
    }

    private String optimizeSPARQL(String code) { // remove duplicate SPARQL filter criteria
        String[] lines = code.split("\n");
        Hashtable htable = new Hashtable();
        String result = "";
        for (int a = 0; a < lines.length; a++) {
            String[] items = lines[a].trim().split(" ");
            boolean markReject = false;
            if (lines[a].contains("|") && !lines[a].trim().startsWith("#")) {
                markReject = true;
            }
            for (int b = 0; b < items.length; b++) {
                String item = items[b].trim();
                if (item.startsWith("?")) {
                    System.out.println(item);
                    int count = StringUtils.countMatches(code.replaceAll("\n", " "), item);
                    if (count == 1) {
                        markReject = true;
                    }
                }
            }
            if (!markReject) {
                result += lines[a] + "\n";
            } else {
                //result += "  # " + lines[a].trim() + "\n";
            }
        }
        return result.replaceAll("\n\n\n+", "\n\n");
    }

    public boolean isNumeric(String s) {
        return s != null && s.matches("-?\\d+(\\.\\d+)?");
    }

    private String makeSPARQL(String in) {
        return in.replaceAll(" ", "_").replaceAll("#", "");
    }

    void setRelativeConstraints(String relativeTemporalCriteria) {
        this.relativeTemporalCriteria = relativeTemporalCriteria;
    }
}
