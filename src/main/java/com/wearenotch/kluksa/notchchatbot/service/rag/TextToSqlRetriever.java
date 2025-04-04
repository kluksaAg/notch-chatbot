package com.wearenotch.kluksa.notchchatbot.service.rag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wearenotch.kluksa.notchchatbot.repository.UserRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class TextToSqlRetriever implements DocumentRetriever {
    private final ChatClient chatClient;

    private final UserRepository userRepository;

    public TextToSqlRetriever(final ChatClient.Builder chatClientBuilder,
                              final UserRepository userRepository) {
        this.chatClient = chatClientBuilder.build();
        this.userRepository = userRepository;
    }

    @Override
    public List<Document> retrieve(Query userQuery) {
        // Convert user query to SQL
        final List<Query> queries = expandQuery(userQuery);
        final List<Query> sqlQueries = queries.stream()
            .map(q -> q.mutate().text(convertTextToSql(q.text())))
            .map(Query.Builder::build)
            .filter(q -> !q.text().equals("<NONE>"))
            .distinct()
            .toList();

        //        final List<Query> queries = List.of(userQuery);
        List<Document> documents = new ArrayList<>();
        for (Query q : sqlQueries) {
            // Execute SQL query
            final List<Map<String, Object>> objects = userRepository.executeSqlQuery(q.text());
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                String json = objectMapper.writeValueAsString(objects);
                if (json.equals("[]")) {
                    continue;
                }
                String answerPrompt = """
                Convert the following SQL query result into a human-readable answer:
            
                Question: "%s"
                JSON Result: "%s"
            
                Respond in a conversational format. Do not explain your choices or add any other text.
            """.formatted(userQuery, json);
                final String content = chatClient.prompt(answerPrompt).call().content();

                documents.add(new Document(json));
                documents.add(new Document(content));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return documents;
    }

    private List<Query> expandQuery(Query query) {
        String templateString = """
            You are an expert at information retrieval and search optimization. 
            Your task is to generate multiple questions based on the given question that will query data source
            that contains only employee data name, date of birth and salary, nothing else. 
            You should ignore the part of the question that is not related to this data.
            You should analyze the original query and create questions that are specific to the data source.
            
            You should provide only concise specific questions. Do not explain your choices or add any other text.
            Provide the query variants separated by newline. If you can not provide a valid question, return newline.
            
            Original query: {query}
            
            Query variants:
            %s
            """.formatted(query.text());
        String generatedResponse = chatClient.prompt(templateString).call().content().trim();
        // Expand the query to include additional information
        return Arrays.stream(generatedResponse.split("\n"))
            .filter(s -> !s.isBlank())
            .map(s -> Query.builder()
                .history(query.history())
                .context(query.context())
                .text(s)
                .build())
            .toList();
    }

    public String convertTextToSql(String query) {
        // Prompt LLM to generate SQL
        String sqlPrompt = """
                You are an intelligent assistant that provides SQL queries based on natural language questions.
               The database schema contains a table with following DDL:
            
                create table public.employees
                (
                    id            serial primary key,
                    name          varchar(255) not null,
                    date_of_birth date,
                    salary        numeric(10, 2)
                );
               Where 
               name: the name of the employee, the search should be case-insensitive and match a part of a name
               date_of_birth: date of birth in PostgreSQL format
               salary: monthly gross salary in EURs
            
               From the provided question below, generate a SQL query that retrieves the relevant data from the employees table.
               Use PostgreSQL dialect syntax.
               The select should return all columns for the employee that matches the name in the question.
            
               If the query would require inserting, updating, deleting, or modifying the schema, respond with 
               <NONE>.
               If the query is not valid or cannot be answered with the given schema, respond with <NONE>.
            
               Query: "%s"
            
            """.formatted(query);

        String generatedSQL = chatClient.prompt(sqlPrompt).call().content().trim();
        if (generatedSQL.startsWith("```sql")) {
            generatedSQL = generatedSQL.replaceAll("\\n", " ");
            generatedSQL = generatedSQL.substring(6);
            generatedSQL = generatedSQL.substring(0, generatedSQL.length() - 3);
        }

        return generatedSQL;
    }
}
