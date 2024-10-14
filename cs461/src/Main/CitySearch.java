package Main;

import java.io.*;
import java.util.*;

public class CitySearch {
    // A map to hold city coordinates (latitude, longitude)
    private static Map<String, double[]> cityCoordinates = new HashMap<>();
    // A graph representation of the cities and their adjacencies
    private static Map<String, List<String>> adjacencyGraph = new HashMap<>();

    class Coordinates {
        double latitude;
        double longitude;

        Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public static void main(String[] args) {
    	// Pass the filename here
        readCoordinates("coordinates.csv"); 
        readAdjacencies("Adjacencies.txt"); 

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter the starting town: ");
            String startCity = scanner.nextLine().trim();
            System.out.print("Enter the ending town: ");
            String endCity = scanner.nextLine().trim();
            // Validates that the cities exist in the system
            if (!cityCoordinates.containsKey(startCity) || !cityCoordinates.containsKey(endCity)) {
                System.out.println("Invalid cities. Please enter valid city names.");
                continue;
            }
            // user promts
            System.out.println("Select a search method:");
            System.out.println("1. Breadth-first search");
            System.out.println("2. Depth-first search");
            System.out.println("3. ID-DFS search");
            System.out.println("4. Best-first search");
            System.out.println("5. A* search");

            int method = Integer.parseInt(scanner.nextLine().trim());
            // starts the timeing for the search
            long startTime = System.currentTimeMillis();
            List<String> path = null;
            // executable for the type of search the user wants 
            switch (method) {
                case 1:
                    path = bfs(startCity, endCity);
                    break;
                case 2:
                    path = dfs(startCity, endCity);
                    break;
                case 3:
                    path = idDfs(startCity, endCity);
                    break;
                case 4:
                    path = bestFirstSearch(startCity, endCity);
                    break;
                case 5:
                    path = aStarSearch(startCity, endCity);
                    break;
                default:
                	// Error message to let the user know their input was wrong
                    System.out.println("Invalid method selection. Please enter a valid method number (1-5).");
                    continue;
            }
            // Ends the time for the search
            long endTime = System.currentTimeMillis();
            if (path != null) {
                System.out.println("Route found: " + String.join(" -> ", path));
                double totalDistance = 0;
                // calculates the distance
                for (int i = 0; i < path.size() - 1; i++) {
                    totalDistance += haversine(cityCoordinates.get(path.get(i)), cityCoordinates.get(path.get(i + 1)));
                }
                System.out.printf("Total distance: %.2f km%n", totalDistance);
                System.out.printf("Time taken: %.4f seconds%n", (endTime - startTime) / 1000.0);
            } else {
                System.out.println("No route found.");
            }

            System.out.print("Do you want to search again? (yes/no): ");
            String choice = scanner.nextLine().trim();
            if (!choice.equalsIgnoreCase("yes")) {
                break;
            }
        }
        scanner.close();
    }

    // Read coordinates from the CSV file
    public static void readCoordinates(String filename) { 
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) { 
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String cityName = parts[0].trim();
                double lat = Double.parseDouble(parts[1].trim());
                double lon = Double.parseDouble(parts[2].trim());
                cityCoordinates.put(cityName, new double[]{lat, lon}); // Store coordinates as an array

                // Debugging output
                //System.out.println("Loaded city: " + cityName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Read adjacencies from the text file
    public static void readAdjacencies(String filename) { 
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) { 
            String line;
            while ((line = br.readLine()) != null) {
                String[] cities = line.trim().split("\\s+"); // Split by whitespace
                String city1 = cities[0];
                String city2 = cities[1];

                adjacencyGraph.putIfAbsent(city1, new ArrayList<>());
                adjacencyGraph.putIfAbsent(city2, new ArrayList<>());

                adjacencyGraph.get(city1).add(city2);
                adjacencyGraph.get(city2).add(city1);

                // Debugging output
                //System.out.println("Added edge: " + city1 + " <-> " + city2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Calculate the Haversine distance between two coordinates
    private static double haversine(double[] coord1, double[] coord2) {
        double lat1 = coord1[0];
        double lon1 = coord1[1];
        double lat2 = coord2[0];
        double lon2 = coord2[1];
        final double R = 6371.0; // Earth's radius in kilometers

        double dlat = Math.toRadians(lat2 - lat1);
        double dlon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // Breadth-first search
    private static List<String> bfs(String start, String end) {
        Set<String> visited = new HashSet<>();
        Queue<Pair> queue = new LinkedList<>();
        queue.offer(new Pair(start, new ArrayList<>()));

        while (!queue.isEmpty()) {
            Pair pair = queue.poll();
            String current = pair.city;
            List<String> path = pair.path;

            if (current.equals(end)) {
                path.add(current);
                return path;
            }
            if (!visited.contains(current)) {
                visited.add(current);
                for (String neighbor : adjacencyGraph.getOrDefault(current, new ArrayList<>())) {
                    if (!visited.contains(neighbor)) {
                        List<String> newPath = new ArrayList<>(path);
                        newPath.add(current);
                        queue.offer(new Pair(neighbor, newPath));
                    }
                }
            }
        }
        return null;
    }

    // Depth-first search
    private static List<String> dfs(String start, String end) {
        Set<String> visited = new HashSet<>();
        Stack<Pair> stack = new Stack<>();
        stack.push(new Pair(start, new ArrayList<>()));

        while (!stack.isEmpty()) {
            Pair pair = stack.pop();
            String current = pair.city;
            List<String> path = pair.path;

            if (current.equals(end)) {
                path.add(current);
                return path;
            }
            if (!visited.contains(current)) {
                visited.add(current);
                for (String neighbor : adjacencyGraph.getOrDefault(current, new ArrayList<>())) {
                    if (!visited.contains(neighbor)) {
                        List<String> newPath = new ArrayList<>(path);
                        newPath.add(current);
                        stack.push(new Pair(neighbor, newPath));
                    }
                }
            }
        }
        return null;
    }

    // ID-DFS search (Iterative Deepening Depth-First Search)
    private static List<String> idDfs(String start, String end) {
        int depth = 0;
        while (true) {
            List<String> result = dfsRecursive(start, end, depth, new HashSet<>());
            if (result != null) {
                return result;
            }
            depth++;
        }
    }

    private static List<String> dfsRecursive(String current, String end, int depth, Set<String> visited) {
        if (depth == 0) {
            return current.equals(end) ? new ArrayList<>(Collections.singletonList(current)) : null;
        }
        visited.add(current);
        for (String neighbor : adjacencyGraph.getOrDefault(current, new ArrayList<>())) {
            if (!visited.contains(neighbor)) {
                List<String> result = dfsRecursive(neighbor, end, depth - 1, visited);
                if (result != null) {
                    result.add(0, current); // Add current city to the path
                    return result;
                }
            }
        }
        visited.remove(current); // Allow for other paths to use this node
        return null;
    }


 // Best-first search (Greedy search)
    private static List<String> bestFirstSearch(String start, String end) {
        Set<String> visited = new HashSet<>();
        PriorityQueue<PriorityPair> heap = new PriorityQueue<>(Comparator.comparingDouble(a -> a.heuristic));
        heap.offer(new PriorityPair(haversine(cityCoordinates.get(start), cityCoordinates.get(end)), start, new ArrayList<>()));

        while (!heap.isEmpty()) {
            PriorityPair pair = heap.poll();
            String current = pair.city;
            List<String> path = pair.path;

            if (current.equals(end)) {
                path.add(current);
                return path;
            }
            if (!visited.contains(current)) {
                visited.add(current);
                for (String neighbor : adjacencyGraph.getOrDefault(current, new ArrayList<>())) {
                    if (!visited.contains(neighbor)) {
                        double h = haversine(cityCoordinates.get(neighbor), cityCoordinates.get(end));
                        List<String> newPath = new ArrayList<>(path);
                        newPath.add(current);
                        heap.offer(new PriorityPair(h, neighbor, newPath));
                    }
                }
            }
        }
        return null;
    }

 // A* search
    private static List<String> aStarSearch(String start, String end) {
        Set<String> visited = new HashSet<>();
        PriorityQueue<AStarPair> heap = new PriorityQueue<>(Comparator.comparingDouble(a -> a.f));
        heap.offer(new AStarPair(0, haversine(cityCoordinates.get(start), cityCoordinates.get(end)), start, new ArrayList<>()));

        while (!heap.isEmpty()) {
            AStarPair pair = heap.poll();
            String current = pair.city;
            List<String> path = pair.path;

            if (current.equals(end)) {
                path.add(current);
                return path;
            }
            if (!visited.contains(current)) {
                visited.add(current);
                for (String neighbor : adjacencyGraph.getOrDefault(current, new ArrayList<>())) {
                    if (!visited.contains(neighbor)) {
                        double g = path.size() + 1; // Cost from start to current node
                        double h = haversine(cityCoordinates.get(neighbor), cityCoordinates.get(end)); // Heuristic cost
                        double f = g + h; // Total cost (f = g + h)
                        List<String> newPath = new ArrayList<>(path);
                        newPath.add(current);
                        heap.offer(new AStarPair(f, h, neighbor, newPath));
                    }
                }
            }
        }
        return null;
    }

 // Pair class for DFS and BFS
    private static class Pair {
        String city;
        List<String> path;

        Pair(String city, List<String> path) {
            this.city = city;
            this.path = path;
        }
    }

    // Priority Pair class for Best-First Search
    private static class PriorityPair {
        double heuristic;
        String city;
        List<String> path;

        PriorityPair(double heuristic, String city, List<String> path) {
            this.heuristic = heuristic;
            this.city = city;
            this.path = path;
        }
    }

    // A* Search Pair class
    private static class AStarPair {
        double f;
        double heuristic;
        String city;
        List<String> path;

        AStarPair(double f, double heuristic, String city, List<String> path) {
            this.f = f;
            this.heuristic = heuristic;
            this.city = city;
            this.path = path;
        }
    }
}
