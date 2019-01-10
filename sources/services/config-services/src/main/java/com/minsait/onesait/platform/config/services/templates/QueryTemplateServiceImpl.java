/**
 * Copyright minsait by Indra Sistemas, S.A.
 * 2013-2018 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.config.services.templates;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.QueryTemplate;
import com.minsait.onesait.platform.config.repository.QueryTemplateRepository;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;

@Slf4j
@Service
public class QueryTemplateServiceImpl implements QueryTemplateService {

    @Autowired
    private QueryTemplateRepository queryTemplateRepository;

    @Override
    public PlatformQuery getTranslatedQuery(String ontology, String query) {
        List<QueryTemplate> templates = queryTemplateRepository.findByOntologyIdentification(ontology);
        MatchResult result = new MatchResult();
        result.setResult(false);
        QueryTemplate template = null;
        
        try {
            for (int i = 0; i < templates.size() && !result.isMatch(); i++) {
                template = templates.get(i);
                result = SqlComparator.match(query, template.getQuerySelector());
            }
            
            if (result.isMatch()) {                
                String newStringQuery = processQuery(template, result);                                
                PlatformQuery newQuery = new PlatformQuery(newStringQuery, template.getType());
                return newQuery;
            }
        } catch (ScriptException | JSQLParserException | NoSuchMethodException e) {
            log.error("Error matching query template", e);
            return null;
        }
        return null;
        
    }
    
    private String processQuery(QueryTemplate template, MatchResult result) throws NoSuchMethodException, ScriptException {
        String query = replaceVariables(template.getQueryGenerator(), result.getVariables());
        query = processQuery(query, template.getName());
        return query;
    }

    
    //It is not private to allow testing
    //TODO test it
    String replaceVariables(String queryGenerator,
            Map<String, VariableData> variables) {
        
        String newQuery = queryGenerator;
        Set<String> variableNames = variables.keySet();
        for (String variableName : variableNames) {
            VariableData variable = variables.get(variableName);
            String newValue = variable.getStringValue();            
            newQuery = newQuery.replace("@"+variableName, newValue);
        }
        
        return newQuery;
    }
    
    //It is not private to allow testing
    //TODO test it
    String processQuery(String query, String templateName) throws ScriptException, NoSuchMethodException {
        final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            final String scriptPostprocessFunction = "function postprocess(){ " + query + " }";
            final ByteArrayInputStream scriptInputStream = new ByteArrayInputStream(scriptPostprocessFunction.getBytes(StandardCharsets.UTF_8));
            engine.eval(new InputStreamReader(scriptInputStream));
            final Invocable inv = (Invocable) engine;
            Object result;
            result = inv.invokeFunction("postprocess");
            return result.toString();
        } catch (final ScriptException e) {
            log.trace("Error processing query in query template: " + templateName, e);
            throw e;
        } catch (NoSuchMethodException e) {
            log.trace("Error invoking processing function in query template: " + templateName, e);
            throw e;
        }
    }
    
    
}
