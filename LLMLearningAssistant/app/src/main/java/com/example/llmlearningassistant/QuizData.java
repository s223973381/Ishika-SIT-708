package com.example.llmlearningassistant;

import java.util.HashMap;
import java.util.Map;

public class QuizData {

    public static class Question {
        public final String text;
        public final String[] options;
        public final int correctIndex;

        public Question(String text, String[] options, int correctIndex) {
            this.text = text;
            this.options = options;
            this.correctIndex = correctIndex;
        }
    }

    private static final Map<String, Question[]> BANK = new HashMap<>();

    static {
        BANK.put("Algorithms", new Question[]{
            new Question("What is the time complexity of Binary Search?",
                new String[]{"O(n)", "O(log n)", "O(n²)", "O(1)"}, 1),
            new Question("Binary Search requires the array to be:",
                new String[]{"Unsorted", "Sorted", "Empty", "Circular"}, 1),
            new Question("Which algorithm uses a divide-and-conquer strategy?",
                new String[]{"Bubble Sort", "Insertion Sort", "Merge Sort", "Selection Sort"}, 2)
        });

        BANK.put("Data Structures", new Question[]{
            new Question("Which data structure follows LIFO order?",
                new String[]{"Queue", "Stack", "Linked List", "Tree"}, 1),
            new Question("Which data structure uses enqueue and dequeue operations?",
                new String[]{"Stack", "Queue", "Graph", "Heap"}, 1),
            new Question("What is the time complexity of accessing an element in an array?",
                new String[]{"O(n)", "O(log n)", "O(1)", "O(n²)"}, 2)
        });

        BANK.put("Web Development", new Question[]{
            new Question("What does HTML stand for?",
                new String[]{"Hyper Text Markup Language", "High Tech Modern Language",
                             "Hyper Transfer Markup Language", "Home Tool Markup Language"}, 0),
            new Question("Which CSS property changes text colour?",
                new String[]{"font-color", "text-color", "color", "foreground"}, 2),
            new Question("Which HTTP method is used to send data to a server?",
                new String[]{"GET", "DELETE", "POST", "HEAD"}, 2)
        });

        BANK.put("Testing", new Question[]{
            new Question("What does TDD stand for?",
                new String[]{"Test Driven Development", "Test Data Design",
                             "Total Deployment Design", "Test Dependency Diagram"}, 0),
            new Question("Which type of testing tests individual units of code?",
                new String[]{"Integration Testing", "System Testing", "Unit Testing", "Regression Testing"}, 2),
            new Question("A test that verifies how different modules work together is called:",
                new String[]{"Unit Test", "Integration Test", "Smoke Test", "Load Test"}, 1)
        });

        BANK.put("Machine Learning", new Question[]{
            new Question("Which of the following is a supervised learning algorithm?",
                new String[]{"K-Means", "Linear Regression", "DBSCAN", "PCA"}, 1),
            new Question("What does 'overfitting' mean?",
                new String[]{"Model performs well on training data but poorly on new data",
                             "Model performs poorly on all data",
                             "Model is too simple",
                             "Model trains too slowly"}, 0),
            new Question("Which metric measures classification accuracy?",
                new String[]{"Mean Squared Error", "R² Score", "F1 Score", "RMSE"}, 2)
        });

        BANK.put("Databases", new Question[]{
            new Question("What does SQL stand for?",
                new String[]{"Structured Query Language", "Simple Query Logic",
                             "Sequential Query List", "Standard Query Layer"}, 0),
            new Question("Which SQL command retrieves data from a table?",
                new String[]{"INSERT", "UPDATE", "SELECT", "DELETE"}, 2),
            new Question("A primary key in a database table must be:",
                new String[]{"Nullable", "Unique and not null", "Duplicated", "A string only"}, 1)
        });

        BANK.put("Networking", new Question[]{
            new Question("What does HTTP stand for?",
                new String[]{"HyperText Transfer Protocol", "High Tech Transfer Process",
                             "Host Transfer Text Protocol", "Hyperlink Text Transfer Process"}, 0),
            new Question("Which layer of the OSI model handles routing?",
                new String[]{"Physical", "Data Link", "Network", "Transport"}, 2),
            new Question("What is the default port for HTTPS?",
                new String[]{"80", "21", "443", "8080"}, 2)
        });

        BANK.put("Operating Systems", new Question[]{
            new Question("What is a deadlock in OS?",
                new String[]{"A process that runs too fast",
                             "When two or more processes wait for each other indefinitely",
                             "A memory overflow error",
                             "When the CPU is idle"}, 1),
            new Question("Which scheduling algorithm gives the shortest job first?",
                new String[]{"FIFO", "Round Robin", "SJF", "Priority Scheduling"}, 2),
            new Question("Virtual memory allows a system to:",
                new String[]{"Run programs faster",
                             "Use disk space as extra RAM",
                             "Share CPU between processes",
                             "Compress files automatically"}, 1)
        });

        BANK.put("Software Engineering", new Question[]{
            new Question("What does SDLC stand for?",
                new String[]{"Software Design and Logic Cycle", "Software Development Life Cycle",
                             "System Deployment and Launch Cycle", "Software Debugging and Launch Check"}, 1),
            new Question("Which methodology delivers software in small increments?",
                new String[]{"Waterfall", "Agile", "V-Model", "Big Bang"}, 1),
            new Question("A UML diagram used to show system use cases is called:",
                new String[]{"Class Diagram", "Sequence Diagram", "Use Case Diagram", "Activity Diagram"}, 2)
        });

        BANK.put("Mobile Development", new Question[]{
            new Question("What language is primarily used for native Android development?",
                new String[]{"Swift", "Kotlin / Java", "Dart", "C#"}, 1),
            new Question("What is an Activity in Android?",
                new String[]{"A background service", "A single screen with a UI", "A database helper", "A network call"}, 1),
            new Question("Which file defines app permissions in Android?",
                new String[]{"build.gradle", "MainActivity.java", "AndroidManifest.xml", "strings.xml"}, 2)
        });

        BANK.put("Cybersecurity", new Question[]{
            new Question("What does SQL injection do?",
                new String[]{"Speeds up database queries",
                             "Inserts malicious SQL code to manipulate a database",
                             "Creates a backup of the database",
                             "Encrypts database entries"}, 1),
            new Question("What does HTTPS provide over HTTP?",
                new String[]{"Faster speed", "Encrypted communication", "Larger bandwidth", "More HTTP methods"}, 1),
            new Question("A brute force attack works by:",
                new String[]{"Guessing all possible passwords until correct",
                             "Stealing session cookies",
                             "Injecting scripts into a web page",
                             "Intercepting network traffic"}, 0)
        });

        BANK.put("Cloud Computing", new Question[]{
            new Question("What does IaaS stand for?",
                new String[]{"Internet as a Service", "Infrastructure as a Service",
                             "Integration as a Solution", "Interface and System"}, 1),
            new Question("Which of the following is a cloud provider?",
                new String[]{"Oracle DB", "AWS", "Android Studio", "GitHub Desktop"}, 1),
            new Question("What is auto-scaling in cloud computing?",
                new String[]{"Automatically resizing images",
                             "Automatically adjusting resources based on demand",
                             "Compressing data automatically",
                             "Updating software automatically"}, 1)
        });
    }

    /** Returns 3 questions for the given topic, falling back to Algorithms if unknown. */
    public static Question[] getQuestions(String topic) {
        Question[] q = BANK.get(topic);
        return q != null ? q : BANK.get("Algorithms");
    }

    /** Returns a one-line description for the topic card on the Home screen. */
    public static String getDescription(String topic) {
        Map<String, String> desc = new HashMap<>();
        desc.put("Algorithms", "Binary Search, sorting, and divide-and-conquer strategies.");
        desc.put("Data Structures", "Stacks, queues, arrays, and linked lists.");
        desc.put("Web Development", "HTML, CSS, HTTP, and frontend fundamentals.");
        desc.put("Testing", "TDD, unit tests, and integration testing.");
        desc.put("Machine Learning", "Supervised learning, overfitting, and model metrics.");
        desc.put("Databases", "SQL queries, primary keys, and relational databases.");
        desc.put("Networking", "HTTP, OSI layers, and network protocols.");
        desc.put("Operating Systems", "Processes, scheduling, and virtual memory.");
        desc.put("Software Engineering", "SDLC, Agile, and UML diagrams.");
        desc.put("Mobile Development", "Android Activities, Manifest, and Kotlin/Java.");
        desc.put("Cybersecurity", "SQL injection, HTTPS, and attack types.");
        desc.put("Cloud Computing", "IaaS, cloud providers, and auto-scaling.");
        String d = desc.get(topic);
        return d != null ? d : "Explore key concepts in " + topic + ".";
    }
}
