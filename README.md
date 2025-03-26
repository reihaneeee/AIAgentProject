# AIAgentProject
In this project, I have built an AIAgent application that gets from the user tasks and its metadata excluding the task's name, deadline, importance, preferred time to do it ,and... and based on some other data such as in which part of the day the used is moreproductivee or the importance of the task it automatically schedules the tasks in trello.com in the boards and also in the calendar.
actually in this application we sent the data to an LLM (in this project I used a free llm: llama 3.2) through a prompt and then got the json to respond of the LLM and then extracted the data we wanted and then sent them to the Trello using API calls.
The other future that exists is that every time the user gives the tasks and their dependencies we build a graph automatically using neo4j to be able to see the whole data in a visual mode to understand it better.  Each node consists of the task data such as its name, deadline, importance and...
We use this also using API calls.
Also, there is something else I worked on; a retrieval system that keeps the data in it for the llm, because to get better answers every time.

The thing What I have learned by doing this project:
1. using API calls and how they work and why and when we use them
   
2. interacting with different large language models(LLM) and sending prompts to them using API calls receiving their JSON responses and how parse the JSON response in a specific way that we need in our project and extracting the data we want.
   
3. prompt engineering; I have tried many prompts to make the LLM give me a good and full answer that I want and learned how should i ask questions from LLMs to give you
a good answer and make sure that its answers are not hilustration are correct.

4. how to use docker
   
5. how to use neo4j; and learn the query language to work with it which is Cypher
   
6.also learned about different databases(especially vector databases and graph databases) and their privilege and when should i use graph and vector databases and 
when I should not.

7. learned about howLLMss work and how can I use them as an agent in my programs
    
8. I learned about the retrieval systems and how they can help us to get better answers.
