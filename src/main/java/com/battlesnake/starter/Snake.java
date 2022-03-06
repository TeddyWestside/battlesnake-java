package com.battlesnake.starter;

import com.battlesnake.starter.Structure.Battlesnake;
import com.battlesnake.starter.Structure.Board;
import com.battlesnake.starter.Structure.Coord;
import com.battlesnake.starter.Structure.GameState;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.lang.reflect.Array;
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
        String lastMove;

        public Map<String, String> process(Request req, Response res) {
            try {
                JsonNode parsedRequest = JSON_MAPPER.readTree(req.body());
                String uri = req.uri();
                LOG.info("{} called with: {}", uri, req.body());
                Map<String, String> snakeResponse;
                switch (uri) {
                    case "/":
                        snakeResponse = index();
                        break;
                    case "/start":
                        snakeResponse = start(parsedRequest);
                        break;
                    case "/move":
                        snakeResponse = move(parsedRequest);
                        break;
                    case "/end":
                        snakeResponse = end(parsedRequest);
                        break;
                    default:
                        throw new IllegalAccessError("Strange call made to the snake: " + uri);
                }

                return snakeResponse;
            } catch (JsonProcessingException e) {
                LOG.warn("Something went wrong!", e);
                return null;
            }
        }

        public Map<String, String> index() {
            Map<String, String> response = new HashMap<>();
            response.put("apiversion", "1");
            response.put("author", "TeddysJavasnake"); // TODO: Your Battlesnake Username
            response.put("color", "#314152"); // TODO: Personalize
            response.put("head", "bendr"); // TODO: Personalize
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
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                gameState = mapper.readValue(moveRequest.toString(), GameState.class);
            } catch (JsonProcessingException e) {
                LOG.info("Data: {}", moveRequest.toString());
                throw e;
            }

            ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));

            // Avoid Obstacles
            avoidMyNeck(gameState.you.head, gameState.you.body, possibleMoves);
            avoidCollisionWithBorders(gameState.you, gameState.board, possibleMoves);
            avoidCollisionWithSnake(gameState.you, gameState.board, possibleMoves);
            LOG.info("after Collision possiblemoves: {}", possibleMoves);


            // TODO: Using information from 'moveRequest', make your Battlesnake move
            // towards a
            // piece of food on the board

            findFood(gameState.you.head, gameState.board, possibleMoves);
            LOG.info("after findFood possiblemoves: {}", possibleMoves);

            String move;

//            if (lastMove != null && possibleMoves.contains(lastMove)) {
//                move = lastMove;
//            } else {
                // Choose a random direction to move in
                final int choice = new Random().nextInt(possibleMoves.size());
                move = possibleMoves.get(choice);
//            }

            LOG.info("MOVE {}", move);

            lastMove = move;
            Map<String, String> response = new HashMap<>();
            response.put("move", move);
            return response;
        }

        public void findFood(Coord head, Board board, ArrayList<String> possibleMoves) {
            if (board.food.length == 0) return;
            Coord nearestFood = getNearestFood(head, board);
            String step = calculateStepTowardsFood(head, nearestFood);
            if (step != null && possibleMoves.contains(step)) {
                possibleMoves.clear();
                possibleMoves.add(step);
            }
        }

        private String calculateStepTowardsFood(Coord head, Coord nearestFood) {
            int xDifference = head.x - nearestFood.x;
            int yDifference = head.y - nearestFood.y;

            if (xDifference > yDifference && xDifference < 0) return "right";
            if (xDifference > yDifference && xDifference > 0) return "left";
            if (xDifference < yDifference && yDifference < 0) return "up";
            if (xDifference < yDifference && xDifference > 0) return "down";
            return null;
        }

        private Coord getNearestFood(Coord head, Board board) {
            ArrayList<Double> euclideanDistanceArray = new ArrayList<>();
            LOG.info("getNearestFood");
            for (int i = 0; i < board.food.length; i++) {
                euclideanDistanceArray.add(Math.sqrt(Math.pow(board.food[i].x - head.x, 2) + Math.pow(board.food[i].y - head.y, 2)));
            }
            double minEuclideanDistance = Double.MAX_EXPONENT;
            LOG.info("euclideanDistanceArray filled {}", euclideanDistanceArray);
            for (Double aDouble : euclideanDistanceArray) {
                if (aDouble < minEuclideanDistance) {
                    minEuclideanDistance = aDouble;
                }
            }
            LOG.info("minEuclideanDistance filled");
            int indexOfMin = euclideanDistanceArray.indexOf(minEuclideanDistance);
            LOG.info("indexOfMin filled {}", indexOfMin);
            LOG.info("board.food[indexOfMin] is {}", board.food[indexOfMin]);
            return board.food[indexOfMin];
        }

        public void avoidCollisionWithSnake(Battlesnake you, Board board, ArrayList<String> possibleMoves) {
            if (checkCoordInUse(you.head.x + 1, you.head.y, board.snakes)) possibleMoves.remove("right");
            if (checkCoordInUse(you.head.x - 1, you.head.y, board.snakes)) possibleMoves.remove("left");
            if (checkCoordInUse(you.head.x, you.head.y + 1, board.snakes)) possibleMoves.remove("up");
            if (checkCoordInUse(you.head.x, you.head.y - 1, board.snakes)) possibleMoves.remove("down");
        }

        private boolean checkCoordInUse(int x, int y, Battlesnake[] snakes) {
            boolean returnValue = false;
            for (Battlesnake snake : snakes) {
                List<Coord> snakeBody = Arrays.asList(snake.body);
                if (snakeBody.stream().anyMatch(o -> o.x == x && o.y == y)) returnValue = true;
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

        public void avoidCollisionWithBorders(Battlesnake you, Board board, ArrayList<String> possibleMoves) {
            Coord head = you.head;

            if (head.x == 0) possibleMoves.remove("left");
            if (head.x == (board.width - 1)) possibleMoves.remove("right");
            if (head.y == 0) possibleMoves.remove("down");
            if (head.y == (board.height - 1)) possibleMoves.remove("up");
        }
    }
}