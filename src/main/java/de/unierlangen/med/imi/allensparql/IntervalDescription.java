package de.unierlangen.med.imi.allensparql;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author matesn
 */
class IntervalDescription {

    public String getOrigDescription() {
        return origDescription;
    }

    public void setOrigDescription(String origDescription) {
        this.origDescription = origDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOccuranceModifier() {
        return occuranceModifier;
    }

    public void setOccuranceModifier(String occuranceModifier) {
        this.occuranceModifier = occuranceModifier;
    }

    public String getValueModifier() {
        return valueModifier;
    }

    public void setValueModifier(String valueModifier) {
        this.valueModifier = valueModifier;
    }

    public String getIntervalName() {
        return intervalName;
    }

    public void setintervalName(String intervalName) {
        this.intervalName = intervalName;
    }

    public String getEnumerator() {
        return enumerator;
    }

    public void setEnumerator(String enumerator) {
        this.enumerator = enumerator;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    private String origDescription = "";
    private String description = "";
    private String occuranceModifier = "";
    private String valueModifier = "";
    private String intervalName = "";
    private String enumerator = "";
    private String itemType = "";
    private long durationSeconds = 0;

    public IntervalDescription(String string) {

        origDescription = string;
        description = origDescription;
        // description = description.replaceAll("> ", ">");
        // description = description.replaceAll("= ", "=");
        // description = description.replaceAll("< ", "<");
        // description = description.replaceAll("! ", "!");
        // description = description.replaceAll("  ", " ");

        String[] items = description.split(" ");

        if (isComparison(items[0])) {
            occuranceModifier = items[0];
        }

        if (items[0].trim().equals("NO")) {
            occuranceModifier = "NO";
        }

        String toProcess = description;

        if (!occuranceModifier.equals("")) {
            toProcess = "";
            for (int a = 1; a < items.length; a++) {
                toProcess += items[a].trim() + " ";
            }
            toProcess = toProcess.trim();
        }
        toProcess = toProcess.replaceAll("  ", " ");

        //System.out.println(">>>  " + toProcess);
        if (toProcess.contains("<") || toProcess.contains("=") || toProcess.contains(">") || toProcess.contains("!")) {
            String temp = toProcess.replaceAll("!", "|").replaceAll("=", "|").replaceAll(">", "|").replaceAll("<", "|")
                    .replaceAll("\\|\\|", "|");
            String[] tp = temp.split("\\|");
            intervalName = tp[0].trim();
            itemType = intervalName;
            // valueModifier = toProcess.replaceAll(intervalName, "").trim();

            valueModifier = "";

            boolean OK = false;
            for (int a = 0; a < toProcess.length(); a++) {
                String c = toProcess.substring(a, a + 1);

                // Start adding after one of the following symbols is found:
                if (c.equals("<") || c.equals(">") || c.equals("=") || c.equals("!")) {
                    OK = true;
                }

                if (OK) {
                    valueModifier += c;
                }

            }
            valueModifier = valueModifier.trim();

        } else {
            intervalName = toProcess.trim();
            itemType = intervalName;
        }

        valueModifier = cleanUpComparator(valueModifier);
        occuranceModifier = cleanUpComparator(occuranceModifier);

        if (intervalName.contains(" ")) {
            String[] tp = intervalName.split(" ");
            if ((tp[tp.length - 1].startsWith("#"))) {
                enumerator = tp[tp.length - 1].trim();
                itemType = intervalName.replaceAll(enumerator, "").trim();
            }
        }

        if (isRelativeInterval(origDescription)) {

            int k = 0;
            if ((items.length & 1) == 0) {
                k = items.length;
            } else {
                k = items.length - 1;
            }

            for (int a = 0; a < k; a = a + 2) {

                String cnt = items[a];
                String unit = items[a + 1];

                if (unit.toLowerCase().contains("second")) {
                    durationSeconds += (Double.parseDouble(cnt));
                }
                if (unit.toLowerCase().contains("minute")) {
                    durationSeconds += (Double.parseDouble(cnt) * 60);
                }
                if (unit.toLowerCase().contains("hour")) {
                    durationSeconds += (Double.parseDouble(cnt) * 60 * 60);
                }
                if (unit.toLowerCase().contains("day")) {
                    durationSeconds += (Double.parseDouble(cnt) * 60 * 60 * 24);
                }
                if (unit.toLowerCase().contains("week")) {
                    durationSeconds += (Double.parseDouble(cnt) * 60 * 60 * 24 * 7);
                }
                if (unit.toLowerCase().contains("month")) {
                    durationSeconds += ((Double.parseDouble(cnt) * 60.0 * 60.0 * 24.0 * 365.25) / 12.0);
                }
                if (unit.toLowerCase().contains("year")) {
                    durationSeconds += (Double.parseDouble(cnt) * 60.0 * 60.0 * 24.0 * 365.25);
                }
            }
        }

        /*
		// occuranceModifier = cleanUpComparator(occuranceModifier);
		// valueModifier = cleanUpComparator(valueModifier);
		System.out.println("");
		System.out.println("Evaluating new interval \"" + description + "\":");
		System.out.println("intervalName     : " + intervalName);
		System.out.println("itemType         : " + itemType);
		System.out.println("enumerator       : " + enumerator);
		System.out.println("occuranceModifier: " + getOccuranceModifier());
		System.out.println("valueModifier    : " + getValueModifier());
		System.out.println("   comparator    : " + getComparator(getValueModifier()));
		System.out.println("   value         : " + getValue(getValueModifier()));
		System.out.println("durationSeconds  : " + durationSeconds);
         */
    }

    private String cleanUpComparator(String input) {
        return (getComparator(input).trim() + " " + getValue(input).trim()).trim();
    }

    public boolean isNumeric(String s) {
        return s != null && s.matches("-?\\d+(\\.\\d+)?");
    }

    public boolean isComparison(String s) {
        return s != null && s.matches("[-+><\\=!]\\d*\\.?\\d+");
    }

    public String buildName() {
        String temp = occuranceModifier + " " + itemType + " " + enumerator + " " + valueModifier;
        temp = temp.replaceAll("  ", " ");
        return temp.trim();
    }

    private void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public boolean isNegation() {
        //System.out.println(occuranceModifier);

        if (occuranceModifier.equals("NO")) {
            return true;
        }
        return false;
    }

    public boolean isComparison() {
        if (!occuranceModifier.equals("") || !valueModifier.equals("")) {
            return true;
        }
        return false;
    }

    public boolean isDuration() {
        if (getDurationSeconds() != 0) {
            return true;
        }
        return false;
    }

    public boolean isTimeStamp() {

        // Based on:
        // https://stackoverflow.com/questions/5902310/how-do-i-validate-a-timestamp
        SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd");
        try {
            format.parse(origDescription);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isRelativeInterval(String in) {
        in = in.toLowerCase();
        if (in.contains(" year") || in.contains(" month") || in.contains(" week") || in.contains(" day")
                || in.contains(" hour") || in.contains(" minute") || in.contains(" second")) {
            return true;
        }
        return false;
    }

    String getIntervallNameSPARQL() {
        return getIntervalName().replaceAll("#", "").replaceAll(" ", "_");
    }

    private String splitComparison(int toGet, String input) {
        String comp = "";
        String value = "";
        for (int a = 0; a < input.length(); a++) {
            String c = input.substring(a, a + 1);
            if (c.equals("<") || c.equals(">") || c.equals("=") || c.equals("!")) {
                comp += c;
            } else {
                value += c;
            }
        }
        if (toGet == 0) {
            return comp.trim();
        } else {
            return value.trim();
        }
    }

    public String getComparator(String input) {
        return splitComparison(0, input);
    }

    public String getValue(String input) {
        return splitComparison(1, input);
    }

    /**
     * @return the durationSeconds
     */
    public long getDurationSeconds() {
        return durationSeconds;
    }

}
