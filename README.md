# AIAgentProject
In this project i have built an AIAgent application that gets from the user tasks and its metadata excluding task's name, deadline, importance, prefereded time to do it and... and based on some other data such as in wich part of the day the used is more prodoctive or the importance of the task it automatically schedules the tasks in trello.com in the boards and aslo in the calender.
acctually in this application we sent the data to an llm (in this project i used a free llm: llama 3.2) through a prompt and then get the json respond of the llm and then extract the data we want and then sent them to the trello using API calls.
the other future that exist is that every time the user gives the tasks and its dependencies we built a graph automatically using neo4j to be able to see the hole data in a visual mode to understand it better.each node consist of the task's data such as its name, deadline, importance and....
we use this also using API calls.
also there is something else i worked on; a retrival system that keeps the data in it for the llm, because to get better answeres every time.

the thing that i have learned by doing this project:
1.using API call and how its work and why and when do we use them
2.intracting with different large language models(LLM) and send prompts to them using API call and 
recive the json responds of them and how to parse the json responde in a speciall way that we need in out project and 
extract the data we want.
3.prompt engineering; i have tried many prompts to make the llm give me a good and full answere that i want and learned how should i ask quesiton from llm's to give you
a good answere and make sure that its answeres are not hilustration and acctullay are correct.
4.how to use docker
5.how to use neo4j; and leared the query language to work with it which is Cypher
6.also learned about different databases(especially about the vector databases and graph databases) and their privilege and when should i use graph and vectpr databeses and 
then i should not.
7.learned about how LLM's work and how can i use it as an agent in my programs
8.I learned about the retrival systems and how they can help us to get better answeres.
