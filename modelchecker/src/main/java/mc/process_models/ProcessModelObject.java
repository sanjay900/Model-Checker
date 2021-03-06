package mc.process_models;

import com.rits.cloning.Cloner;
import com.rits.cloning.IDeepCloner;
import com.rits.cloning.IFastCloner;
import lombok.Getter;
import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public abstract class ProcessModelObject implements Serializable {

    // fields
    private String id;
    @Getter
    private String type;
    @Getter
    private Map<String, Object> metaData;

    public ProcessModelObject(String id, String type){
        this.id = id;
        this.type = type;
        this.metaData = new HashMap<String, Object>();
    }

    public String getId(){
        return id;
    }

    public Object getMetaData(String key){
        if(metaData.containsKey(key)){
            return metaData.get(key);
        }

        return null;
    }

    public void addMetaData(String key, Object value){
        metaData.put(key, value);
    }

    public void removeMetaData(String key){
        if(metaData.containsKey(key)){
            metaData.remove(key);
        }
    }

    public boolean hasMetaData(String key){
        return metaData.containsKey(key);
    }

    public Set<String> getMetaDataKeys(){
        return metaData.keySet();
    }

    public ProcessModelObject copy() throws CompilationException {
        return new Cloner().deepClone(this);
    }
}
