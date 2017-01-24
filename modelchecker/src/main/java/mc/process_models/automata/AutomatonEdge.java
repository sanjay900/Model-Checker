package mc.process_models.automata;

import mc.Constant;
import mc.process_models.ProcessModelObject;

/**
 * Created by sheriddavi on 24/01/17.
 */
public class AutomatonEdge extends ProcessModelObject {

    private String label;
    private AutomatonNode from;
    private AutomatonNode to;

    public AutomatonEdge(String id, String label, AutomatonNode from, AutomatonNode to){
        super(id);
        this.label = label;
        this.from = from;
        this.to = to;
    }

    public String getLabel(){
        return label;
    }

    public void setLabel(String label){
        this.label = label;
    }

    public AutomatonNode getFrom(){
        return from;
    }

    public void setFrom(AutomatonNode from){
        this.from = from;
    }

    public AutomatonNode getTo(){
        return to;
    }

    public void setTo(AutomatonNode to){
        this.to = to;
    }

    public boolean isHidden(){
        return label.equals(Constant.HIDDEN);
    }

    public boolean isDeadlocked(){
        return label.equals(Constant.DEADLOCK);
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("edge{\n");
        builder.append("\tid:" + getId() + "\n");
        builder.append("\tlabel:" + label + "\n");
        builder.append("\tfrom:" + from.getId() + "\n");
        builder.append("\tto:" + to.getId() + "\n");
        builder.append("}");

        return builder.toString();
    }
}