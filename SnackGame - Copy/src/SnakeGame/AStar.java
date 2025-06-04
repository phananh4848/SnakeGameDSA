package SnakeGame;

import java.util.*;

public class AStar {
    public static List<Point> findPath(Point start, Point goal, Set<Point> obstacles, int width, int height, int unitSize) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<Point, Point> cameFrom = new HashMap<>();
        Map<Point, Integer> gScore = new HashMap<>();

        Node startNode = new Node(start, 0, heuristic(start, goal));
        openSet.add(startNode);
        gScore.put(start, 0);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.point.equals(goal)) {
                return reconstructPath(cameFrom, current.point);
            }

            for (Point neighbor : getNeighbors(current.point, width, height, unitSize)) {
                if (obstacles.contains(neighbor)) continue;
                int tentativeG = gScore.getOrDefault(current.point, Integer.MAX_VALUE) + 1;
                if (tentativeG < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    cameFrom.put(neighbor, current.point);
                    gScore.put(neighbor, tentativeG);
                    int f = tentativeG + heuristic(neighbor, goal);
                    openSet.add(new Node(neighbor, tentativeG, f));
                }
            }
        }

        return null;
    }

    private static List<Point> reconstructPath(Map<Point, Point> cameFrom, Point current) {
        List<Point> totalPath = new ArrayList<>();
        while (cameFrom.containsKey(current)) {
            totalPath.add(0, current);
            current = cameFrom.get(current);
        }
        return totalPath;
    }

    private static int heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private static List<Point> getNeighbors(Point point, int width, int height, int unitSize) {
        List<Point> neighbors = new ArrayList<>();
        int[] dx = {-unitSize, unitSize, 0, 0};
        int[] dy = {0, 0, -unitSize, unitSize};

        for (int i = 0; i < 4; i++) {
            int nx = point.x + dx[i];
            int ny = point.y + dy[i];
            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                neighbors.add(new Point(nx, ny));
            }
        }

        return neighbors;
    }

    static class Node {
        Point point;
        int g, f;

        Node(Point point, int g, int f) {
            this.point = point;
            this.g = g;
            this.f = f;
        }
    }
}
