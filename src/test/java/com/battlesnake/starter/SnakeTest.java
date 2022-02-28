package com.battlesnake.starter;

import com.battlesnake.starter.Structure.Coord;
import com.battlesnake.starter.Structure.GameState;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SnakeTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    private Snake.Handler handler;

    @BeforeEach
    void setUp() {
        handler = new Snake.Handler();
    }

    @Test
    void indexTest() throws IOException {

        Map<String, String> response = handler.index();
        assertEquals("#314152", response.get("color"));
        assertEquals("bendr", response.get("head"));
        assertEquals("default", response.get("tail"));
    }

    @Test
    void startTest() throws IOException {
        JsonNode startRequest = OBJECT_MAPPER.readTree("{}");
        Map<String, String> response = handler.end(startRequest);
        assertEquals(0, response.size());

    }

//    @Test
//    void moveTest() throws IOException {
//        JsonNode moveRequest = OBJECT_MAPPER.readTree(
//                "{\"game\":{\"id\":\"game-00fe20da-94ad-11ea-bb37\",\"ruleset\":{\"name\":\"standard\",\"version\":\"v.1.2.3\"},\"timeout\":500},\"turn\":14,\"board\":{\"height\":11,\"width\":11,\"food\":[{\"x\":5,\"y\":5},{\"x\":9,\"y\":0},{\"x\":2,\"y\":6}],\"hazards\":[{\"x\":3,\"y\":2}],\"snakes\":[{\"id\":\"snake-508e96ac-94ad-11ea-bb37\",\"name\":\"My Snake\",\"health\":54,\"body\":[{\"x\":0,\"y\":0},{\"x\":1,\"y\":0},{\"x\":2,\"y\":0}],\"latency\":\"111\",\"head\":{\"x\":0,\"y\":0},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"},{\"id\":\"snake-b67f4906-94ae-11ea-bb37\",\"name\":\"Another Snake\",\"health\":16,\"body\":[{\"x\":5,\"y\":4},{\"x\":5,\"y\":3},{\"x\":6,\"y\":3},{\"x\":6,\"y\":2}],\"latency\":\"222\",\"head\":{\"x\":5,\"y\":4},\"length\":4,\"shout\":\"I'm not really sure...\",\"squad\":\"\"}]},\"you\":{\"id\":\"snake-508e96ac-94ad-11ea-bb37\",\"name\":\"My Snake\",\"health\":54,\"body\":[{\"x\":0,\"y\":0},{\"x\":1,\"y\":0},{\"x\":2,\"y\":0}],\"latency\":\"111\",\"head\":{\"x\":0,\"y\":0},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}}");
//
//        ObjectMapper mapper = new ObjectMapper();
//        GameState gameState = mapper.readValue(moveRequest.asText(), GameState.class);
//        Map<String, String> response = handler.move(gameState);
//
//        List<String> options = new ArrayList<>();
//        options.add("up");
//        options.add("down");
//        options.add("left");
//        options.add("right");
//
//        assertTrue(options.contains(response.get("move")));
//    }

    @Test
    void endTest() throws IOException {
        JsonNode endRequest = OBJECT_MAPPER.readTree("{}");
        Map<String, String> response = handler.end(endRequest);
        assertEquals(0, response.size());
    }

    @Test
    void avoidNeckAllTest() throws IOException {

        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");
        JsonNode testBody = OBJECT_MAPPER
                .readTree("[{\"x\": 5, \"y\": 5}, {\"x\": 5, \"y\": 5}, {\"x\": 5, \"y\": 5}]");
        ObjectMapper headMapper = new ObjectMapper();
        Coord head = headMapper.readValue(testHead.toString(), Coord.class);
        ObjectMapper bodyMapper = new ObjectMapper();
        Coord[] body = bodyMapper.readValue(testBody.toString(), Coord[].class);

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));

        handler.avoidMyNeck(head, body, possibleMoves);

        assertEquals(4, possibleMoves.size());
        assertEquals(possibleMoves, expectedResult);
    }

    @Test
    void avoidNeckLeftTest() throws IOException {

        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");
        JsonNode testBody = OBJECT_MAPPER
                .readTree("[{\"x\": 5, \"y\": 5}, {\"x\": 4, \"y\": 5}, {\"x\": 3, \"y\": 5}]");
        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "down", "right"));

        ObjectMapper headMapper = new ObjectMapper();
        Coord head = headMapper.readValue(testHead.toString(), Coord.class);
        ObjectMapper bodyMapper = new ObjectMapper();
        Coord[] body = bodyMapper.readValue(testBody.toString(), Coord[].class);


        handler.avoidMyNeck(head, body, possibleMoves);


        assertEquals(3, possibleMoves.size());
        assertEquals(possibleMoves, expectedResult);
    }

    @Test
    void avoidNeckRightTest() throws IOException {

        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");
        JsonNode testBody = OBJECT_MAPPER
                .readTree("[{\"x\": 5, \"y\": 5}, {\"x\": 6, \"y\": 5}, {\"x\": 7, \"y\": 5}]");
        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "down", "left"));

        ObjectMapper headMapper = new ObjectMapper();
        Coord head = headMapper.readValue(testHead.toString(), Coord.class);
        ObjectMapper bodyMapper = new ObjectMapper();
        Coord[] body = bodyMapper.readValue(testBody.toString(), Coord[].class);

        handler.avoidMyNeck(head, body, possibleMoves);

        assertEquals(3, possibleMoves.size());
        assertEquals(possibleMoves, expectedResult);
    }

    @Test
    void avoidNeckUpTest() throws IOException {

        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");
        JsonNode testBody = OBJECT_MAPPER
                .readTree("[{\"x\": 5, \"y\": 5}, {\"x\": 5, \"y\": 6}, {\"x\": 5, \"y\": 7}]");
        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("down", "left", "right"));

        ObjectMapper headMapper = new ObjectMapper();
        Coord head = headMapper.readValue(testHead.toString(), Coord.class);
        ObjectMapper bodyMapper = new ObjectMapper();
        Coord[] body = bodyMapper.readValue(testBody.toString(), Coord[].class);

        handler.avoidMyNeck(head, body, possibleMoves);

        assertEquals(3, possibleMoves.size());
        assertEquals(possibleMoves, expectedResult);
    }

    @Test
    void avoidNeckDownTest() throws IOException {

        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");
        JsonNode testBody = OBJECT_MAPPER
                .readTree("[{\"x\": 5, \"y\": 5}, {\"x\": 5, \"y\": 4}, {\"x\": 5, \"y\": 3}]");
        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "left", "right"));

        ObjectMapper headMapper = new ObjectMapper();
        Coord head = headMapper.readValue(testHead.toString(), Coord.class);
        ObjectMapper bodyMapper = new ObjectMapper();
        Coord[] body = bodyMapper.readValue(testBody.toString(), Coord[].class);

        handler.avoidMyNeck(head, body, possibleMoves);

        assertEquals(3, possibleMoves.size());
        assertEquals(possibleMoves, expectedResult);
    }

    @Test
    void MapResponseCorrectly() throws JsonProcessingException {
        JsonNode response = OBJECT_MAPPER.readTree("{\"game\":{\"id\":\"38d13cc4-eed4-4691-9784-1df38834658f\",\"ruleset\":{\"name\":\"solo\",\"version\":\"v1.0.25\",\"settings\":{\"foodSpawnChance\":0,\"minimumFood\":0,\"hazardDamagePerTurn\":0,\"royale\":{\"shrinkEveryNTurns\":0},\"squad\":{\"allowBodyCollisions\":false,\"sharedElimination\":false,\"sharedHealth\":false,\"sharedLength\":false}}},\"timeout\":500,\"source\":\"challenge\"},\"turn\":2,\"board\":{\"height\":5,\"width\":5,\"snakes\":[{\"id\":\"gs_YWXGS3SJVKgM3wbjDXGPChSX\",\"name\":\"TeddysJavasnake\",\"latency\":\"17\",\"health\":98,\"body\":[{\"x\":2,\"y\":4},{\"x\":2,\"y\":3},{\"x\":2,\"y\":2}],\"head\":{\"x\":2,\"y\":4},\"length\":3,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#b00b69\",\"head\":\"default\",\"tail\":\"default\"}}],\"food\":[],\"hazards\":[]},\"you\":{\"id\":\"gs_YWXGS3SJVKgM3wbjDXGPChSX\",\"name\":\"TeddysJavasnake\",\"latency\":\"17\",\"health\":98,\"body\":[{\"x\":2,\"y\":4},{\"x\":2,\"y\":3},{\"x\":2,\"y\":2}],\"head\":{\"x\":2,\"y\":4},\"length\":3,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#b00b69\",\"head\":\"default\",\"tail\":\"default\"}}}");

        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        GameState gameState = mapper.readValue(response.toString(), GameState.class);
        assertEquals(gameState.game.id, "38d13cc4-eed4-4691-9784-1df38834658f");
    }

    @Test
    void dontMoveDownWhenCollisionWithSnake() throws JsonProcessingException {
        // given
        JsonNode response = OBJECT_MAPPER.readTree("{\"game\":{\"id\":\"0462f820-bf27-468b-b076-c85d0139c6e2\",\"ruleset\":{\"name\":\"solo\",\"version\":\"v1.0.25\",\"settings\":{\"foodSpawnChance\":0,\"minimumFood\":0,\"hazardDamagePerTurn\":0,\"royale\":{\"shrinkEveryNTurns\":0},\"squad\":{\"allowBodyCollisions\":false,\"sharedElimination\":false,\"sharedHealth\":false,\"sharedLength\":false}}},\"timeout\":500,\"source\":\"challenge\"},\"turn\":33,\"board\":{\"height\":11,\"width\":11,\"snakes\":[{\"id\":\"gs_7GtkCPSvwC7GmpGBFjCrRHSJ\",\"name\":\"TeddysJavasnake\",\"latency\":\"19\",\"health\":67,\"body\":[{\"x\":2,\"y\":7},{\"x\":1,\"y\":7},{\"x\":1,\"y\":6},{\"x\":2,\"y\":6},{\"x\":3,\"y\":6}],\"head\":{\"x\":2,\"y\":7},\"length\":5,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#b00b69\",\"head\":\"default\",\"tail\":\"default\"}}],\"food\":[],\"hazards\":[]},\"you\":{\"id\":\"gs_7GtkCPSvwC7GmpGBFjCrRHSJ\",\"name\":\"TeddysJavasnake\",\"latency\":\"19\",\"health\":67,\"body\":[{\"x\":2,\"y\":7},{\"x\":1,\"y\":7},{\"x\":1,\"y\":6},{\"x\":2,\"y\":6},{\"x\":3,\"y\":6}],\"head\":{\"x\":2,\"y\":7},\"length\":5,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#b00b69\",\"head\":\"default\",\"tail\":\"default\"}}}");
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        GameState gameState = mapper.readValue(response.toString(), GameState.class);


        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "right"));

        //act
        handler.avoidCollisionWithSnake(gameState.you, gameState.board, possibleMoves);

        //assert
        assertEquals(expectedResult, possibleMoves);

    }
}
