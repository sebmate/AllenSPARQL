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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author matesn
 */
class SPARQLTranslator {

    String Allen = "";

    HashSet<String> additionalVariables = new HashSet<String>();

    ArrayList<AllenStatement> allenStatements = new ArrayList<AllenStatement>();
    private String relativeTemporalCriteria;

    SPARQLTranslator(String Allen) {
        String[] AllenStatementsInput = Allen.split(";");
        for (int a = 0; a < AllenStatementsInput.length; a++) {
            //System.out.println(AllenStatementsInput[a]);
            AllenStatement as = new AllenStatement(AllenStatementsInput[a]);
            allenStatements.add(as);
        }
    }

    String getQueryPart(IntervalDescription interval1, String relation, IntervalDescription interval2, int exclQueryNumber) {

        relation = relation.replaceAll(",_", "|a:");

        String result = "";
        String conceptIdentifier1 = interval1.getIntervallNameSPARQL();
        String conceptIdentifier2 = interval2.getIntervallNameSPARQL();
        String IntervalPrefix = "int_";

        String indent = "";
        if (exclQueryNumber > 0) {
            conceptIdentifier1 += "_excl_" + exclQueryNumber;
            conceptIdentifier2 += "_excl_" + exclQueryNumber;
            indent = "  ";
        }

        //if (!interval1.isNegation() && !interval2.isNegation()) {
        String caption = "===[ " + interval1.getDescription() + " " + relation.replaceAll("a:", "") + " " + interval2.getDescription();
        if (exclQueryNumber > 0) {
            caption += " (Part of exclusion #" + exclQueryNumber + ")";
        }

        caption += " ]===";
        result += "\n" + indent + "  # ";
        String filled = "";
        if (exclQueryNumber > 0) {
            filled = StringUtils.repeat("=", (99 - 5 - 2 - caption.length()) / 2);
        } else {
            filled = StringUtils.repeat("=", (99 - 5 - caption.length()) / 2);
        }

        result += filled + caption + filled + "\n";

        if (!interval1.isDuration() && !interval2.isDuration()) {
            result += "\n" + indent + "  ?" + IntervalPrefix + conceptIdentifier1 + " a:" + relation + " ?" + IntervalPrefix + conceptIdentifier2 + " . \n";
        } else {
            result += "\n" + indent + "  # Note: No direct constraint for this Allen statement was added, because at least one of the\n";
            result += "" + indent + "  #       two intervals is a duration. Relative temporal constraints will be handled at the end.\n";
        }
        //}

        //if ((interval1.isNegation() || interval2.isNegation())/* && !(interval1.isNegation() && interval2.isNegation())*/) {
        //    String caption = "===[ " + interval1.getDescription() + " " + relation.replaceAll("a:", "") + " " + interval2.getDescription() + " ]===";
        //    result += "\n  # ";
        //    String filled = StringUtils.repeat("=", (99 - 5 - caption.length()) / 2);
        //    result += filled + caption + filled + "\n";
        //    if (!interval1.isDuration() && !interval2.isDuration()) {
        //        result += "\n  FILTER NOT EXISTS {?" + IntervalPrefix + conceptIdentifier1 + " a:" + relation + " ?" + IntervalPrefix + conceptIdentifier2 + "} . \n";
        //    } else {
        //        result += "\n  # ERROR: Cannot process negation (\"NO\" intervals) across relative temporal constraints.\n";
        //    }
        //}
        // Describe interval 1:
        if (!interval1.isDuration()) {
            result += "\n" + indent + "  # Describing the interval \"" + interval1.getIntervalName() + "\":";
            result += "\n" + indent + "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasConcept \"" + interval1.getItemType() + "\" .\n"
                    + "" + indent + "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasPatient ?patient .\n";
            result += "" + indent + "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasValue ?value_of_" + IntervalPrefix + conceptIdentifier1 + " .\n";
            result += "" + indent + "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasStartDateUnix ?unix_start_of_" + IntervalPrefix + conceptIdentifier1 + " .\n";
            result += "" + indent + "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasEndDateUnix ?unix_end_of_" + IntervalPrefix + conceptIdentifier1 + " .\n";
            result += "" + indent + "  # ?" + IntervalPrefix + conceptIdentifier1 + " a:hasStartDate ?start_of_" + IntervalPrefix + conceptIdentifier1 + " .\n";
            result += "" + indent + "  # ?" + IntervalPrefix + conceptIdentifier1 + " a:hasEndDate ?end_of_" + IntervalPrefix + conceptIdentifier1 + " .\n";
            additionalVariables.add("?start_of_" + IntervalPrefix + conceptIdentifier1);
            additionalVariables.add("?end_of_" + IntervalPrefix + conceptIdentifier1);

        }

        // Describe interval 2:
        if (!interval2.isDuration()) {
            result += "\n" + indent + "  # Describing the interval \"" + interval2.getIntervalName() + "\":";
            result += "\n" + indent + "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasConcept \"" + interval2.getItemType() + "\" .\n"
                    + "" + indent + "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasPatient ?patient .\n";
            result += "" + indent + "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasValue ?value_of_" + IntervalPrefix + conceptIdentifier2 + " .\n";
            result += "" + indent + "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasStartDateUnix ?unix_start_of_" + IntervalPrefix + conceptIdentifier2 + " .\n";
            result += "" + indent + "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasEndDateUnix ?unix_end_of_" + IntervalPrefix + conceptIdentifier2 + " .\n";
            result += "" + indent + "  # ?" + IntervalPrefix + conceptIdentifier2 + " a:hasStartDate ?start_of_" + IntervalPrefix + conceptIdentifier2 + " .\n";
            result += "" + indent + "  # ?" + IntervalPrefix + conceptIdentifier2 + " a:hasEndDate ?end_of_" + IntervalPrefix + conceptIdentifier2 + " .\n";
            additionalVariables.add("?start_of_" + IntervalPrefix + conceptIdentifier2);
            additionalVariables.add("?end_of_" + IntervalPrefix + conceptIdentifier2);
        }

        result += "\n\n" + indent + "";

        // Value filtering:
        if ((!interval1.getValueModifier().trim().equals(""))) {
            String compValue = "";
            if (isNumeric(interval1.getValue(interval1.getValueModifier()))) {
                compValue = interval1.getValueModifier();
            } else {
                compValue = interval1.getComparator(interval1.getValueModifier()) + " ?value_of_" + IntervalPrefix + makeSPARQL(interval1.getValue(interval1.getValueModifier()));
            }
            result += "" + "  # Constraining the value of the interval \"" + interval1.getIntervalName() + "\":\n";
            result += "" + indent + "  FILTER (" + "?value_of_" + IntervalPrefix + conceptIdentifier1 + " " + compValue + ") .\n\n";
            additionalVariables.add("?value_of_" + IntervalPrefix + conceptIdentifier1);
        }

        if ((!interval2.getValueModifier().trim().equals(""))) {
            String compValue = "";
            if (isNumeric(interval2.getValue(interval2.getValueModifier()))) {
                compValue = interval2.getValueModifier();
            } else {
                compValue = interval2.getComparator(interval2.getValueModifier()) + " ?value_of_" + IntervalPrefix + makeSPARQL(interval2.getValue(interval2.getValueModifier()));
            }
            result += "" + "  # Constraining the value of the interval \"" + interval2.getIntervalName() + "\":\n";
            result += "" + indent + "  FILTER (" + "?value_of_" + IntervalPrefix + conceptIdentifier2 + " " + compValue + ") .\n\n";
            additionalVariables.add("?value_of_" + IntervalPrefix + conceptIdentifier2);
        }

        // Subinterval counting:
        if ((!interval1.getOccuranceModifier().equals("")) && (!interval1.getOccuranceModifier().equals("NO"))) {
            result += "" + "  # Constraining subinterval count of interval \"" + interval1.getIntervalName() + "\":\n";
            result += "" + indent + "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasSubCount ?count_of_" + IntervalPrefix + conceptIdentifier1 + " .\n";
            result += "" + indent + "  FILTER (" + "?count_of_" + IntervalPrefix + conceptIdentifier1 + " " + interval1.getOccuranceModifier() + ") .\n\n";
            additionalVariables.add("?count_of_" + IntervalPrefix + conceptIdentifier1);
        }
        if ((!interval2.getOccuranceModifier().equals("")) && (!interval2.getOccuranceModifier().equals("NO"))) {
            result += "" + indent + "  # Constraining subinterval count of interval \"" + interval2.getIntervalName() + "\":\n";
            result += "" + indent + "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasSubCount ?count_of_" + IntervalPrefix + conceptIdentifier2 + " .\n";
            result += "" + indent + "  FILTER (" + "?count_of_" + IntervalPrefix + conceptIdentifier2 + " " + interval2.getOccuranceModifier() + ") .\n\n";
            additionalVariables.add("?count_of_" + IntervalPrefix + conceptIdentifier2);
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
            if (!comp.equals("") && !interval1.isDuration()) {
                result += "\n" + indent + "  # Constraining the duration of interval \"" + interval1.getIntervalName() + "\" to " + comp + " " + interval2.getIntervalName() + ":\n";
                result += "" + indent + "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasStartDateUnix ?unix_start_of_" + IntervalPrefix + conceptIdentifier1 + " .\n";
                result += "" + indent + "  ?" + IntervalPrefix + conceptIdentifier1 + " a:hasEndDateUnix ?unix_end_of_" + IntervalPrefix + conceptIdentifier1 + " .\n";
                result += "" + indent + "  FILTER (?unix_end_of_" + IntervalPrefix + conceptIdentifier1 + " - ?unix_start_of_" + IntervalPrefix + conceptIdentifier1 + " " + comp + " " + interval2.getDurationSeconds() + ") .\n\n";
            } else {
                // Either the relation is not limiting the length of the interval or both intervals are duration intervals.
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
            if (!comp.equals("") && !interval2.isDuration()) {
                result += "\n" + indent + "  # Constraining the duration of interval \"" + interval2.getIntervalName() + "\" to " + comp + " " + interval1.getIntervalName() + ":\n";
                result += "" + indent + "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasStartDateUnix ?unix_start_of_" + IntervalPrefix + conceptIdentifier2 + " .\n";
                result += "" + indent + "  ?" + IntervalPrefix + conceptIdentifier2 + " a:hasEndDateUnix ?unix_end_of_" + IntervalPrefix + conceptIdentifier2 + " .\n";
                result += "" + indent + "  FILTER (?unix_end_of_" + IntervalPrefix + conceptIdentifier2 + " - ?unix_start_of_" + IntervalPrefix + conceptIdentifier2 + " " + comp + " " + interval1.getDurationSeconds() + ") .\n\n";
            } else {
                // Either the relation is not limiting the length of the interval or both intervals are duration intervals.
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
                + "SELECT DISTINCT ?patient\nWHERE {\n\n";

        //boolean OK = false;
        //String knownIntervals = "";
        Boolean foundStatements = false;
        String thisAllen = "";
        for (int a = 0; a < allenStatements.size(); a++) {
            AllenStatement as = allenStatements.get(a);
            //OK = true;
            String iName1 = as.getInterval1().getIntervalName();
            String iName2 = as.getInterval2().getIntervalName();
            //knownIntervals += iName1 + ";";
            //knownIntervals += iName2 + ";";
            if (!(as.getInterval1().isNegation() || as.getInterval2().isNegation())) { // Exclude statements with negations here 
                result += getQueryPart(as.getInterval1(), as.getRelation(), as.getInterval2(), 0);
                thisAllen += as.getInterval1().getIntervalName() + " [" + as.getRelation() + "] " + as.getInterval2().getIntervalName() + "\n";
                foundStatements = true;
            }
        }

        if (!foundStatements) {
            result += "  # Note: No Allen statements could be created, therefore searching for \"all\" patients:\n\n";
            result += "  ?anyInterval a:hasPatient ?patient .\n\n";
           
        }


        relativeTemporalCriteria = getRelativeTemporalCriteria(thisAllen, 0);
        if (!relativeTemporalCriteria.equals("")) {
            result += "  # ================[ Constraints on relative intervals (derived with SageMath) ]================ \n\n";
            result += relativeTemporalCriteria + "\n\n";
        }

        // Test if there are exclusions:
        Boolean hasExcls = false;
        HashSet<String> negations = new HashSet<String>();
        for (int a = 0; a < allenStatements.size(); a++) {
            AllenStatement as = allenStatements.get(a);
            if (as.getInterval1().isNegation() || as.getInterval2().isNegation()) {
                hasExcls = true;
            }
        }

        if (hasExcls) {
            result += "  # ==================================[ Processing Exclusions ]================================== \n\n";
            result += "  MINUS {\n\n";

            int atExclusion = 1;
            //String knownIntervals = "";
            thisAllen = "";
            for (int a = 0; a < allenStatements.size(); a++) {
                AllenStatement as = allenStatements.get(a);
               // OK = true;
                String iName1 = as.getInterval1().getIntervalName();
                String iName2 = as.getInterval2().getIntervalName();
                //knownIntervals += iName1 + ";";
                //knownIntervals += iName2 + ";";
                if ((!as.getInterval1().isNegation() && !as.getInterval2().isNegation()) || (as.getInterval1().isNegation() && !as.getInterval2().isNegation())
                        || (!as.getInterval1().isNegation() && as.getInterval2().isNegation())) { // Only include statements with negations
                    result += getQueryPart(as.getInterval1(), as.getRelation(), as.getInterval2(), atExclusion);
                    thisAllen += as.getInterval1().getIntervalName() + "_excl_" + atExclusion + " [" + as.getRelation() + "] " + as.getInterval2().getIntervalName() + "_excl_" + atExclusion + "\n";
                }
            }
            relativeTemporalCriteria = getRelativeTemporalCriteria(thisAllen, atExclusion);
            if (!relativeTemporalCriteria.equals("")) {
                result += "    # ================[ Constraints on relative intervals (derived with SageMath) ]================ \n\n";
                result += " " + relativeTemporalCriteria.replaceAll("\n", "\n ") + "\n\n";
            }
            result += "\n  } # End of exclusions\n\n";
        }

        // ---------- This code implements logical OR across exclusions ----------
        /*
        // Collect exclusions:
        HashSet<String> negations = new HashSet<String>();
        for (int a = 0; a < allenStatements.size(); a++) {
            AllenStatement as = allenStatements.get(a);
            if (as.getInterval1().isNegation()) {
                negations.add(as.getInterval1().getDescription());
            }
            if (as.getInterval2().isNegation()) {
                negations.add(as.getInterval2().getDescription());
            }
        }

        Iterator<String> itr2 = negations.iterator();

        while (itr2.hasNext()) {
            String var = itr2.next();
            System.out.println("Excluding: " + var);
            atExclusion++;
            result += "  MINUS { # ==============[ Exclusion #" + atExclusion + ": \"" + var + "\" ]==============\n\n";
            thisAllen = "";
            for (int b = 0; b < allenStatements.size(); b++) {
                AllenStatement as2 = allenStatements.get(b);
                OK = true;
                if (as2.getInterval1().isNegation() && as2.getInterval2().isNegation()) { // Ignore if both are negations
                } else {
                    if (as2.getInterval1().isNegation() && as2.getInterval1().getDescription().equals(var)) {
                        result += getQueryPart(as2.getInterval1(), as2.getRelation(), as2.getInterval2(), atExclusion);
                        thisAllen += "  " + as2.getInterval1().getIntervalName() + "_excl_" + atExclusion + " [" + as2.getRelation() + "] " + as2.getInterval2().getIntervalName() + "_excl_" + atExclusion + "\n";
                    }
                    if (as2.getInterval2().isNegation() && as2.getInterval2().getDescription().equals(var)) {
                        result += getQueryPart(as2.getInterval1(), as2.getRelation(), as2.getInterval2(), atExclusion);
                        thisAllen += "  " + as2.getInterval1().getIntervalName() + "_excl_" + atExclusion + " [" + as2.getRelation() + "] " + as2.getInterval2().getIntervalName() + "_excl_" + atExclusion + "\n";
                    }
                    if (!as2.getInterval1().isNegation() && !as2.getInterval2().isNegation()) {
                        result += getQueryPart(as2.getInterval1(), as2.getRelation(), as2.getInterval2(), atExclusion);
                        thisAllen += "  " + as2.getInterval1().getIntervalName() + "_excl_" + atExclusion + " [" + as2.getRelation() + "] " + as2.getInterval2().getIntervalName() + "_excl_" + atExclusion + "\n";
                    }
                }
            }
            relativeTemporalCriteria = getRelativeTemporalCriteria(thisAllen, 2);
            if (!relativeTemporalCriteria.equals("")) {
                result += "  # ================[ Constraints on relative intervals (derived with SageMath) ]================ \n\n";
                result += relativeTemporalCriteria + "\n\n";
            }
            result += "\n  } # End of exclusion #" + atExclusion + ": \"" + var + "\"\n\n";
        }
         */
        result += "} ORDER BY ?patient";
        result = cleanUpSPARQL(result);
        String addedVariables = "";
        Iterator<String> itr = additionalVariables.iterator();

        while (itr.hasNext()) {
            String var = itr.next();
            if (!var.contains("_excl_")) {
                addedVariables += "  # " + var + "\n";
            }
        }

        result = result.replaceAll("DISTINCT \\?patient", "DISTINCT \\?patient\n\n  # To retrieve additional data, uncomment the variables and associated statements below,\n  # and execute this SPARQL code directly on the Fuseki server:\n\n" + addedVariables + "\n");

        result = optimizeSPARQL(result);

        //    if (!OK) {
        //        return "";
        //    }
        return result;
    }

    private String cleanUpSPARQL(String code) { // remove duplicate SPARQL filter criteria
        String[] lines = code.split("\n");
        Hashtable htable = new Hashtable();
        String result = "";
        for (int a = 0; a < lines.length; a++) {
            if (!htable.contains(lines[a]) || lines[a].trim().equals("") || (lines[a].trim().startsWith("#") && !lines[a].trim().contains("Describing") && !lines[a].trim().contains("Constraining"))) {
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

            //System.out.println("Testing line: " + lines[a]);
            boolean markReject = false;

            if (!lines[a].trim().startsWith("FILTER") && !lines[a].trim().startsWith("?anyInterval") ) {
                String[] items = lines[a].trim().split(" ");

                //if (lines[a].contains("|") && !lines[a].trim().startsWith("#")) {
                //    markReject = true;
                //    System.out.println("### Reject 1");
                //}
                for (int b = 0; b < items.length; b++) {
                    String item = items[b].trim();
                    if (item.startsWith("?")) {
                        //System.out.println("Item: " + item);
                        int count = StringUtils.countMatches(code.replaceAll("\n", " "), item);
                        if (count == 1) {
                            markReject = true;
                            // System.out.println("### Reject 2");
                        }
                    }
                }
            }
            if (!markReject) {
                if ((result.contains(lines[a]) && !lines[a].equals("") && !lines[a].trim().startsWith("#")) || (result.contains(lines[a]) && lines[a].trim().startsWith("#") && lines[a].contains("a:"))) {

                } else {
                    result += lines[a] + "\n";
                }
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

    private String getRelativeTemporalCriteria(String AllenToProcess, int indent) {

        String result = "";
        // Derive relative constraints:
        if (!AllenToProcess.trim().replaceAll(";", "").equals("") && !AllenToProcess.contains("Temporal Error")) {
            System.out.println("Preparing to compute relative temporal constraints via SageMathCell ...");
            RelativeConstraintsProcessor rcp = new RelativeConstraintsProcessor(AllenToProcess);
            String relatives = rcp.process();

            if (!relatives.equals("")) {
                System.out.println("SageMath expression to evaluate is:\n");
                System.out.println(relatives + "\n");
                System.out.println("Calling SageMathCell ...");
                result = rcp.callSageMath(relatives);
                System.out.println("Relative temporal criteria: " + result);
                result = rcp.makeSparqlFilter(result);
                System.out.println("... Done!");

                if (result.equals("")) {

                    System.out.println("SageMathCell did not return a result when computing the \"constraints on relative intervals\" SPARQL section. Please check for invalid temporal patterns involving duration intervals (e.g. \"3 Months\" during \"2 Months\")!");
                    //JOptionPane.showMessageDialog(null, "SageMathCell did not return a result when computing the \"constraints on relative intervals\" SPARQL section.\nPlease check for invalid temporal patterns involving duration intervals (e.g. \"3 Months\" during \"2 Months\")!", "Temporal Error", JOptionPane.ERROR_MESSAGE);
                    result = "  # ERROR: SageMathCell did not return a result when computing the \"constraints on relative\n"
                            + "  #        intervals\" SPARQL section. Please check for invalid temporal patterns involving\n"
                            + "  #        duration intervals (e.g. \"3 Months\" during \"2 Months\")!";
                }
            } else {
                System.out.println("There are no relative temporal constraints in this query.\n");
            }
        }
        String filled = StringUtils.repeat(" ", indent);
        result = filled + result.replaceAll("\n", "\n" + filled);

        return result;
    }
}
