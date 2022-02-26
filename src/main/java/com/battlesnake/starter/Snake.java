package com.battlesnake.starter;

import com.battlesnake.starter.Structure.Battlesnake;
import com.battlesnake.starter.Structure.Board;
import com.battlesnake.starter.Structure.Coord;
import com.battlesnake.starter.Structure.GameState;
import com.fasterxml.jackson.core.JsonProcessingException;
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
                ObjectMapper mapper = new ObjectMapper();
                GameState gameState = mapper.readValue(parsedRequest.asText(), GameState.class);
                String uri = req.uri();
//                LOG.info("{} called with: {}", uri, req.body());
                Map<String, String> snakeResponse;
                switch (uri) {
                    case "/":
                        snakeResponse = index();
                        break;
                    case "/start":
                        snakeResponse = start(parsedRequest);
                        break;
                    case "/move":
                        snakeResponse = move(gameState);
                        break;
                    case "/end":
                        snakeResponse = end(parsedRequest);
                        break;
                    default:
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
         *
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

        public Map<String, String> move(GameState gameState) {

//            try {
//                LOG.info("Data: {}", JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(moveRequest));
//            } catch (JsonProcessingException e) {
//                LOG.error("Error parsing payload", e);
//            }

            ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));

            // Don't allow your Battlesnake to move back in on it's own neck
            possibleMoves = avoidMyNeck(gameState.you.head, gameState.you.body, possibleMoves);

            // TODO: Using information from 'moveRequest', find the edges of the board and
            // don't
            // let your Battlesnake move beyond them board_height = ? board_width = ?
            possibleMoves = this.avoidColisionWithBorders(gameState.you,  gameState.board, possibleMoves);

            // TODO Using information from 'moveRequest', don't let your Battlesnake pick a
            // move
            // that would hit its own body
//            this.avoidColisionWithSelf(gameState.you.head, gameState.you.body, gameState.board, possibleMoves);

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

        private void avoidColisionWithSelf(JsonNode head, JsonNode body, JsonNode board, ArrayList<String> possibleMoves) {
            ArrayList bodies = new ObjectMapper().convertValue(body, ArrayList.class);
        }

        public ArrayList<String> avoidMyNeck(Coord head, Coord[] body, ArrayList<String> possibleMoves) {
            Coord neck = body[1];

            if (neck.x < head.x) possibleMoves.remove("left");
            else if (neck.x > head.x) possibleMoves.remove("right");
            else if (neck.y < head.y) possibleMoves.remove("down");
            else if (neck.y > head.y) possibleMoves.remove("up");
            return possibleMoves;
        }

        public Map<String, String> end(JsonNode endRequest) {
            LOG.info("END");
            return EMPTY;
        }

        public ArrayList<String> avoidColisionWithBorders(Battlesnake you, Board board, ArrayList<String> possibleMoves){
            Coord head = you.head;
            int height = board.height;
            int width = board.width;

            if (head.x == width - 1) possibleMoves.remove("right");
            if (head.x == 0) possibleMoves.remove("left");
            if (head.y == height - 1) possibleMoves.remove("up");
            if (head.y == 0) possibleMoves.remove("down");

            return possibleMoves;
        }
    }
}