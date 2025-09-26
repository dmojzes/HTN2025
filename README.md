<img width="1280" height="640" alt="Untitled design (8)" src="https://github.com/user-attachments/assets/8ff22901-3d06-4147-a1a6-b7dcfbe12f17" /><br>

Glerb: Your Personal AI Assistant Helping You Find and Work With Anything on Your Screen.<br>

What it Does:<br>
The project provides users with a moving HUD that grants direct access to the OpenAI platform. Overall, Glerb allows users to easily take screenshots and receive contextual AI feedback on them with the help of a prompt.<br>

Inspiration:<br>
Glerb was primarily inspired through exploring different use cases of AI in the context of human computer interaction. By providing users with an AI assistant taking multiple types of inputs, it was thought possible to provide people with a more interactive and powerful experience using AI.<br>

Challenges:<br>
Due to the nature of taking screenshots and using AI API keys, many of the development challenges of Glerb were related to computer security. For example, taking screenshots on macOS required special screen recording privileges to be granted to the program. It was also decided that users would provide their own OpenAI API keys.<br>

Running:<br>
1. export OPENAI_API_KEY={your_openai_api_key}<br>

2. mvn package<br>

3. java -cp target/my-app-1.0-SNAPSHOT.jar com.mycompany.app.App<br>

4. Grant screen recording permissions to your IDE
