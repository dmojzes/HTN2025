package com.mycompany.app;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.Random;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import com.google.gson.*;

public class App {
    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");

        String currentWorkingDir = System.getProperty("user.dir");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;
        int offsetX = 300;
        int offsetY = 300;
        Random rand = new Random();
        ImageIcon charImage = new ImageIcon(currentWorkingDir + "/images/glerb.png");
        ImageIcon charImageMove = new ImageIcon(currentWorkingDir + "/images/glerb_move.gif");
        ImageIcon charImageThink = new ImageIcon(currentWorkingDir + "/images/glerb_think.gif");

        SwingUtilities.invokeLater(() -> {
            JFrame hiddenFrame = new JFrame();
            hiddenFrame.setUndecorated(true);
            hiddenFrame.setSize(0, 0);
            hiddenFrame.setVisible(true);

            // Window
            JWindow frame = new JWindow(hiddenFrame);
            frame.setBackground(new Color(0, 0, 0, 0));
            frame.setSize(250, 150);
            frame.setAlwaysOnTop(true);
            frame.setFocusableWindowState(true);
            frame.setLayout(new BorderLayout());
            frame.setLocation(rand.nextInt((width - offsetX) - offsetX + 1) + offsetX, rand.nextInt((height - offsetY) - offsetY + 1) + offsetY);

            // Character button
            JButton charButton = new JButton(charImage);
            charButton.setFocusPainted(false);
            charButton.setBorderPainted(false);
            charButton.setContentAreaFilled(false);
            charButton.setDoubleBuffered(true);
            charButton.putClientProperty("active", false);
            frame.add(charButton, BorderLayout.CENTER);

            // User input panel
            JTextField userInputField = new JTextField(20);
            userInputField.setCaretColor(Color.WHITE);
            userInputField.setForeground(Color.WHITE);
            userInputField.setBackground(new Color(30, 30, 30, 255));
            userInputField.setBorder(BorderFactory.createLineBorder(new Color(64, 64, 64, 255), 2, true));
            userInputField.setBorder(BorderFactory.createCompoundBorder(
                userInputField.getBorder(),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
            ));

            JButton submitButton = new JButton(" → ");
            submitButton.setFont(new Font("Arial", Font.BOLD, 18));
            submitButton.setForeground(Color.WHITE);
            submitButton.setBorder(BorderFactory.createLineBorder(new Color(64, 64, 64, 255), 2, true));
            
            JButton exitButton = new JButton(" x ");
            exitButton.setFont(new Font("Arial", Font.BOLD, 18));
            exitButton.setForeground(Color.WHITE);
            exitButton.setBorder(BorderFactory.createLineBorder(new Color(64, 64, 64, 255), 2, true));

            JPanel inputPanel = new JPanel(new BorderLayout());
            inputPanel.setBackground(new Color(64, 64, 64, 255));
            inputPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 2, true));
            inputPanel.add(userInputField, BorderLayout.CENTER);
            inputPanel.add(exitButton, BorderLayout.WEST);
            inputPanel.add(submitButton, BorderLayout.EAST);
            frame.add(inputPanel, BorderLayout.SOUTH);

            charButton.addActionListener(event -> {
                Boolean active = (Boolean) charButton.getClientProperty("active");
                if (!active) {
                    charButton.putClientProperty("active", true);
                    inputPanel.setVisible(false);
                    frame.revalidate();
                    frame.repaint();

                    int targetX = rand.nextInt((width - offsetX) - offsetX + 1) + offsetX;
                    int targetY = rand.nextInt((height - offsetY) - offsetY + 1) + offsetY;

                    charButton.setIcon(charImageMove);
                    int startX = frame.getX();
                    int startY = frame.getY();

                    int steps = 120;
                    int delay = 2000 / steps;

                    double deltaX = targetX - startX;
                    double deltaY = targetY - startY;

                    final int[] currentStep = {0};

                    Timer timer = new Timer(delay, e -> {
                        double t = (double) currentStep[0] / steps;
                        double easedT = t < 0.5
                                ? 4 * t * t * t
                                : 1 - Math.pow(-2 * t + 2, 3) / 2;

                        int newX = startX + (int)(deltaX * easedT);
                        int newY = startY + (int)(deltaY * easedT);

                        frame.setLocation(newX, newY);

                        currentStep[0]++;
                        if (currentStep[0] > steps) {
                            frame.setLocation(targetX, targetY);
                            ((Timer)e.getSource()).stop();
                            charButton.setIcon(charImage);
                            charButton.putClientProperty("active", false);
                            inputPanel.setVisible(true);
                            frame.revalidate();
                            frame.repaint();
                        }
                    });
                    timer.start();
                }
            });

            userInputField.addActionListener(e -> submitButton.doClick());

            exitButton.addActionListener(event -> {
                Boolean active = (Boolean) charButton.getClientProperty("active");
                if (!active) {
                    System.exit(0);
                }
            });

            submitButton.addActionListener(event -> {
                Boolean active = (Boolean) charButton.getClientProperty("active");
                if (!active && (!userInputField.getText().trim().isEmpty() && (apiKey != null))) {
                    charButton.putClientProperty("active", true);
                    inputPanel.setVisible(false);
                    frame.revalidate();
                    frame.repaint();
                    charButton.setIcon(charImageThink);
                    new Thread(() -> {
                        try {
                            BufferedImage screenshot = new Robot().createScreenCapture(new Rectangle(0, 0, screenSize.width, screenSize.height));

                            File outputFile = new File("screenshot.png");

                            try {
                                ImageIO.write(screenshot, "png", outputFile);
                                System.out.println("Saved screenshot to " + outputFile.getAbsolutePath());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(screenshot, "png", baos);
                            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                            String base64ImageUri = "data:image/png;base64," + base64Image;
                            String prompt = userInputField.getText();
                            String projectGoal = "You are an assistant analyzing a full-screen screenshot that represents the current state of the screen and a user goal: (" + prompt + "). " +
                                "The screenshot has a resolution of " + screenSize.width + "x" + screenSize.height + " pixels, " +
                                "with the top-left corner as (0,0) and bottom-right as (" + (screenSize.width-1) + "," + (screenSize.height-1) + "). " +
                                "Your task is to always provide a single pixel coordinate (x,y) indicating the most relevant point related to the user's goal. " +
                                "Respond in the exact format: x,y,feedback. " +
                                "x and y must be integers within the visible screen area. " +
                                "feedback must be a short, kind, and descriptive sentence within 30 characters. " +
                                "If the user is just asking about the situation, the feedback should explain what the problem is at that point. " +
                                "If the user explicitly asks for a fix, the feedback should instead provide a friendly actionable suggestion about what to do at that pixel. " +
                                "Never omit the coordinates; they are always required.";
                            String jsonPayload = """
                            {
                            "model": "gpt-5-nano",
                            "input": [
                                { "role": "user", "content": [{ "type": "input_text", "text": "%s" }] },
                                { "role": "user", "content": [{ "type": "input_image", "image_url": "%s" }] }
                            ]
                            }
                            """.formatted(projectGoal, base64ImageUri);
                            HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.openai.com/v1/responses"))
                                .header("Content-Type", "application/json")
                                .header("Authorization", "Bearer " + apiKey)
                                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                                .build();
                            HttpClient client = HttpClient.newHttpClient();

                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                            String responseBody = response.body();
                            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                            String feedback = "";
                            JsonArray outputArray = json.getAsJsonArray("output");
                            if (outputArray != null) {
                                for (JsonElement elem : outputArray) {
                                    JsonObject outputObj = elem.getAsJsonObject();
                                    JsonArray content = outputObj.getAsJsonArray("content");
                                    if (content != null) {
                                        for (JsonElement c : content) {
                                            JsonObject cObj = c.getAsJsonObject();
                                            if (cObj.has("text")) {
                                                feedback = cObj.get("text").getAsString();
                                            }
                                        }
                                    }
                                }
                            } else {
                                System.out.println("No output array in response.");
                            }

                            String[] parts = feedback.split(",", 3);
                            int targetX = Integer.parseInt(parts[0].trim());
                            int targetY = Integer.parseInt(parts[1].trim());
                            String textFeedback = parts[2];
                            userInputField.setText(textFeedback);

                            charButton.setIcon(charImageMove);
                            int startX = frame.getX();
                            int startY = frame.getY();

                            int steps = 120;
                            int delay = 2000 / steps;

                            double deltaX = targetX - startX;
                            double deltaY = targetY - startY;

                            final int[] currentStep = {0};

                            Timer timer = new Timer(delay, e -> {
                                double t = (double) currentStep[0] / steps;
                                double easedT = t < 0.5
                                        ? 4 * t * t * t
                                        : 1 - Math.pow(-2 * t + 2, 3) / 2;

                                int newX = startX + (int)(deltaX * easedT);
                                int newY = startY + (int)(deltaY * easedT);

                                frame.setLocation(newX, newY);

                                currentStep[0]++;
                                if (currentStep[0] > steps) {
                                    frame.setLocation(targetX, targetY);
                                    ((Timer)e.getSource()).stop();
                                    charButton.setIcon(charImage);
                                    charButton.putClientProperty("active", false);
                                    inputPanel.setVisible(true);
                                    frame.repaint();
                                }
                            });

                            timer.start();
                        } catch (Exception e) {
                            charButton.setIcon(charImage);
                            charButton.putClientProperty("active", false);
                            inputPanel.setVisible(true);
                            frame.revalidate();
                            frame.repaint();
                            e.printStackTrace();
                        }
                    }).start();
                } else {
                    inputPanel.setVisible(true);
                    frame.revalidate();
                    frame.repaint();
                    System.out.println("Error: API key missing or no input entered.");
                }
            });
            frame.setVisible(true);
        });
    };
}
