# Query-Overflow

This Android project is an all-in-one question-and-answer mobile application, aimed at allowing students to ask or answer any questions and deepen their knowledge base of a topic. This concept came about during a Mobile Application Module in school, under the overarching topic of higher education. self-directed learning is a crucial aspect of it. Unlike the structured environments of primary and secondary education, higher education requires a proactive approach to gain knowledge and master the topics.

## Features

1. **Question-and-Answer Functionality**: The application enables students to pose questions about unfamiliar topics or seek further clarification on course material to create a collaborative environment where everyone can provide answers.
2. **Point System**: Students can ‘like’ questions and answers they approve. The points garnered from the voting allow subsequent students to see the community-approved answers. This feature allows the lectures the potential to reward the top contributors, further incentivising a collaborative learning environment.
3. **Machine Learning**: This feature leverages Google’s GenerativeAI, or Gemini, to develop an intelligent chatbot. The users will then be able to communicate with the “bot” to pose questions and receive insightful responses through machine learning, enhancing the overall learning experience for all students. We will utilise the API key provided in the Gemini 1.0 Pro Model to generate the responses. The decision for the model is deliberate, and the availability of a free tier allows the functionality testing of the chatbot, albeit at a lower limit for API calls per minute.
4. **Comprehensive Local Database**: A comprehensive local database will be implemented using the Room Library to store the chat history with Gemini with multiple tables. The local database and the Data Access Objects and Repository will be utilised to implement this capability fully. The tables will include the message exchange between each user and the model. Ensuring that data is efficiently retrieved and organised.
5. **Translation for Chabot**: A translation functionality allowing the user to translate the message into a chosen language. This will make the chatbot more accessible to users who speak different languages.
6. **Speech-to-Text and Text-to-Speech Conversion**: The chatbot can convert audio speech into text, and the model responses can be converted to audio to be played back to the user. This will make the chatbot accessible to visually impaired users or allow users to have a hands-free experience.
7. **Comprehensive Firebase Storage**: A comprehensive database to store the User’s information, posts and comments will be done on Firebase. The users can log in to the mobile application using Firebase's built-in authentication feature.
8. **Networking**: Data storage will be done on the cloud to provide robust security to the user’s personal data and question data. Automatic weekly backups to our dedicated online server will be utilised to ensure data integrity and accessibility.
9. **Image Storage Capability**: Students can seamlessly attach visual aids to the posts, which will then be stored in the Realtime Database feature within Firebase, allowing for added user experience.

## Technologies Used

1. **Android Studio**: The primary IDE used to develop the Android application.
2. **Java**: The primary programming language used to develop the Android application.
3. **Google’s GenerativeAI**: The machine learning model used to develop the chatbot. **API Key Required**.
4. **Firebase**: The cloud storage service used to store user data, posts, and comments. **API Key Required**.
5. **Room Library**: The library used to create a local database to store the chat history with Gemini.
6. **Google Translate API**: The API used to translate the chatbot’s responses into the user’s chosen language.
7. **Speech-to-Text and Text-to-Speech Conversion**: The feature used to convert audio speech into text and the model responses into audio.

## Installation

1. **Clone the Repository**: Clone the repository using the following command:
   ```
   git clone
    ```
2. **Open the Project**: Open the project in Android Studio.
3. **Run the Application**: Run the application on an emulator or a physical device.