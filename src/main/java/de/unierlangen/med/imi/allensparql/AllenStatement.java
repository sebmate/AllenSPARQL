/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.unierlangen.med.imi.allensparql;

/**
 *
 * @author matesn
 */
class AllenStatement {

    private IntervalDescription interval1;
    private IntervalDescription interval2;
    private String relation = "";
 

    AllenStatement(String in) {
        String[] AllenStatement = in.replaceAll("\\]", ";").replaceAll("\\[", ";").split(";");
        this.relation = AllenStatement[1].trim().replaceAll(" ", "_");

        interval1 = new IntervalDescription(AllenStatement[0].trim());
        interval2 = new IntervalDescription(AllenStatement[2].trim());
    }

    /**
     * @return the interval1
     */
    public IntervalDescription getInterval1() {
        return interval1;
    }

    /**
     * @param interval1 the interval1 to set
     */
    public void setInterval1(IntervalDescription interval1) {
        this.interval1 = interval1;
    }

    /**
     * @return the interval2
     */
    public IntervalDescription getInterval2() {
        return interval2;
    }

    /**
     * @param interval2 the interval2 to set
     */
    public void setInterval2(IntervalDescription interval2) {
        this.interval2 = interval2;
    }

    /**
     * @return the relation
     */
    public String getRelation() {
        return relation;
    }

    /**
     * @param relation the relation to set
     */
    public void setRelation(String relation) {
        this.relation = relation;
    }

    void toPreferred() {
        switch (this.relation) {
            case "before":
                break;
            case "meets":
                break;
            case "overlaps":
                break;
            case "finished_by":
                break;
            case "contains":
                break;
            case "starts":
                break;
            case "equals":
                break;
            case "started_by":
                flip();
                break;
            case "during":
                flip();
                break;
            case "finishes":
                flip();
                break;
            case "overlapped_by":
                flip();
                break;
            case "met_by":
                flip();
                break;
            case "after":
                flip();
                break;
            default:
                System.out.println("Error: Unknown temporal relation '" + this.relation + "'");
                break;
        }

    }

    private void flip() {
        IntervalDescription temp = this.interval1;
        this.interval1 = this.interval2;
        this.interval2 = temp;

        switch (this.relation) {
            case "before":
                this.relation = "after";
                break;
            case "meets":
                this.relation = "met by";
                break;
            case "overlaps":
                this.relation = "overlapped by";
                break;
            case "finished_by":
                this.relation = "finishes";
                break;
            case "contains":
                this.relation = "during";
                break;
            case "starts":
                this.relation = "started by";
                break;
            case "equals":
                this.relation = "equals";
                break;
            case "started_by":
                this.relation = "starts";
                break;
            case "during":
                this.relation = "contains";
                break;
            case "finishes":
                this.relation = "finished by";
                break;
            case "overlapped_by":
                this.relation = "overlaps";
                break;
            case "met_by":
                this.relation = "meets";
                break;
            case "after":
                this.relation = "before";
                break;
            default:
                System.out.println("Error: Unknown temporal relation '" + this.relation + "'");
                break;
        }
    }
}
