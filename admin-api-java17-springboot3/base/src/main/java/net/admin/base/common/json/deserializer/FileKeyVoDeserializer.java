package net.admin.base.common.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import net.admin.base.module.support.file.domain.vo.FileVO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FileKeyVoDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        List<FileVO> list = new ArrayList<>();
        ObjectCodec objectCodec = jsonParser.getCodec();
        JsonNode listOrObjectNode = objectCodec.readTree(jsonParser);
        String deserialize = "";
        try {
            if (listOrObjectNode.isArray()) {
                for (JsonNode node : listOrObjectNode) {
                    list.add(objectCodec.treeToValue(node, FileVO.class));
                }
            } else {
                list.add(objectCodec.treeToValue(listOrObjectNode, FileVO.class));
            }
            deserialize = list.stream().map(FileVO::getFileKey).collect(Collectors.joining(","));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            deserialize = listOrObjectNode.asText();
        }
        return deserialize;
    }


}
