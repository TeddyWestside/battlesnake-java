package com.battlesnake.starter;

import com.battlesnake.starter.Structure.Battlesnake;
import com.battlesnake.starter.Structure.Board;
import com.battlesnake.starter.Structure.Coord;
import com.battlesnake.starter.Structure.GameState;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.*;

import static spark.Spark.*;


public class Snake {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final Handler HANDLER = new Handler();
    private static final Logger LOG = LoggerFactory.getLogger(Snake.class);


    public static void main(String[] args) {
        String port = System.getProperty("PORT");
        if (port == null) {
            LOG.info("Using default port: {}", port);
            port = "8080";
        } else {
            LOG.info("Found system provided port: {}", port);
        }
        port(Integer.parseInt(port));
        get("/", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/start", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/move", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/end", HANDLER::process, JSON_MAPPER::writeValueAsString);
    }

    public static class Handler {

        private static final Map<String, String> EMPTY = new HashMap<>();

        public Map<String, String> process(Request req, Response res) {
            try {
                JsonNode parsedRequest = JSON_MAPPER.readTree(req.body());
                String uri = req.uri();
                LOG.info("{} called with: {}", uri, req.body());
                Map<String, String> snakeResponse;
                if (uri.equals("/")) {
                    snakeResponse = index();
                } else if (uri.equals("/start")) {
                    snakeResponse = start(parsedRequest);
                } else if (uri.equals("/move")) {
                    snakeResponse = move(parsedRequest);
                } else if (uri.equals("/end")) {
                    snakeResponse = end(parsedRequest);
                } else {
                    throw new IllegalAccessError("Strange call made to the snake: " + uri);
                }

//                LOG.info("Responding with: {}", JSON_MAPPER.writeValueAsString(snakeResponse));

                return snakeResponse;
            } catch (JsonProcessingException e) {
                LOG.warn("Something went wrong!", e);
                return null;
            }
        }

        public Map<String, String> index() {
            Map<String, String> response = new HashMap<>();
            response.put("apiversion", "1");
            response.put("author", ""); // TODO: Your Battlesnake Username
            response.put("color", "#B00B69"); // TODO: Personalize
            response.put("head", "default"); // TODO: Personalize
            response.put("tail", "default"); // TODO: Personalize
            return response;
        }

        /**
         * This method is called everytime your Battlesnake is entered into a game.
         * <p>
         * Use this method to decide how your Battlesnake is going to look on the board.
         *
         * @param startRequest a JSON data map containing the information about the game
         *                     that is about to be played.
         * @return responses back to the engine are ignored.
         */
        public Map<String, String> start(JsonNode startRequest) {
            LOG.info("START");
            return EMPTY;
        }

        public Map<String, String> move(JsonNode moveRequest) throws JsonProcessingException {

            GameState gameState;
            try {
                ObjectMapper mapper = new ObjectMapper();
//                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                gameState = mapper.readValue(moveRequest.toString(), GameState.class);
            } catch (JsonProcessingException e) {
                LOG.info("Data: {}", moveRequest.toString());
                throw e;
            }

            ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));

            // Don't allow your Battlesnake to move back in on it's own neck
            avoidMyNeck(gameState.you.head, gameState.you.body, possibleMoves);

            // TODO: Using information from 'moveRequest', find the edges of the board and
            // don't
            // let your Battlesnake move beyond them board_height = ? board_width = ?
            avoidColisionWithBorders(gameState.you, gameState.board, possibleMoves);

            // TODO Using information from 'moveRequest', don't let your Battlesnake pick a
            // move
            // that would hit its own body
            avoidColisionWithSelf(gameState.you, gameState.board, possibleMoves);

            // TODO: Using information from 'moveRequest', don't let your Battlesnake pick a
            // move
            // that would collide with another Battlesnake

            // TODO: Using information from 'moveRequest', make your Battlesnake move
            // towards a
            // piece of food on the board

            // Choose a random direction to move in
            final int choice = new Random().nextInt(possibleMoves.size());
            final String move = possibleMoves.get(choice);

            LOG.info("MOVE {}", move);

            Map<String, String> response = new HashMap<>();
            response.put("move", move);
            return response;
        }

        private void avoidColisionWithSelf(Battlesnake you, Board board, ArrayList<String> possibleMoves) {
            LOG.info("avoidColisionWithSelf you: {}", you.head.x);
            LOG.info("avoidColisionWithSelf board: {}", board.snakes[0].head.x);
            for (int i = 0; i < board.snakes.length; i++) {
                if (checkCoordFree(you.head.x + 1, you.head.y, board.snakes)) possibleMoves.remove("right");
//                if (checkCoordFree(you.head.x - 1, you.head.y, board.snakes)) possibleMoves.remove("left");
//                if (checkCoordFree(you.head.x, you.head.y + 1, board.snakes)) possibleMoves.remove("up");
//                if (checkCoordFree(you.head.x, you.head.y - 1, board.snakes)) possibleMoves.remove("down");
            }
        }

        private boolean checkCoordFree(int x, int y, Battlesnake[] snakes) {
            Coord coord = new Coord(x, y);
            boolean returnValue = true;
            for (Battlesnake snake : snakes) {
                List<Coord> snakeBody = Arrays.asList(snake.body);
                if (snakeBody.contains(coord)) {
                    returnValue = false;
                    break;
                }
            }
            return returnValue;
        }

        public void avoidMyNeck(Coord head, Coord[] body, ArrayList<String> possibleMoves) {
            Coord neck = body[1];

            if (neck.x < head.x) possibleMoves.remove("left");
            else if (neck.x > head.x) possibleMoves.remove("right");
            else if (neck.y < head.y) possibleMoves.remove("down");
            else if (neck.y > head.y) possibleMoves.remove("up");
        }

        public Map<String, String> end(JsonNode endRequest) {
            LOG.info("END");
            return EMPTY;
        }

        public void avoidColisionWithBorders(Battlesnake you, Board board, ArrayList<String> possibleMoves) {
            Coord head = you.head;

            if (head.x == 0) possibleMoves.remove("left");
            if (head.x == (board.width - 1)) possibleMoves.remove("right");
            if (head.y == 0) possibleMoves.remove("down");
            if (head.y == (board.height - 1)) possibleMoves.remove("up");
        }
    }
}