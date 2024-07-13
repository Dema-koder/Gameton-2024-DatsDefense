package ru.demyan;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
class Target {
    private int x;
    private int y;

    public Target(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
@Data
class AttackingBlock {
    private String blockId;
    private Target target;

    public AttackingBlock(String blockId, Target target) {
        this.blockId = blockId;
        this.target = target;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }

    // Method to create a Block object from JSON
    public static AttackingBlock fromJson(String json) throws JsonMappingException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, AttackingBlock.class);
    }
}
