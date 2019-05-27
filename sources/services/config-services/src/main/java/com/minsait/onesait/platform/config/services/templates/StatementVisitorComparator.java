/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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

import net.sf.jsqlparser.statement.Block;
import net.sf.jsqlparser.statement.Commit;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UseStatement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

public class StatementVisitorComparator implements StatementVisitor{

    private MatchResult result;
    private Statement otherStatement;
    
    public StatementVisitorComparator(Statement otherStatement, MatchResult result) {
        this.result = result;
        this.otherStatement = otherStatement;
    }
    
    @Override
    public void visit(Commit commit) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Delete delete) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Update update) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Insert insert) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Replace replace) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Drop drop) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Truncate truncate) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(CreateIndex createIndex) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(CreateTable createTable) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(CreateView createView) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(AlterView alterView) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Alter alter) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Statements stmts) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Execute execute) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(SetStatement set) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Merge merge) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Select select1) {
        boolean sameClass = true;
        Select select2 = null;
        try {
            select2 = (Select) otherStatement;
        } catch (ClassCastException e) {
            sameClass = false;
        }
        if (sameClass) {
            SqlComparator.matchSelect(select1,  select2, result);
        } else {
            result.setResult(false);
        }
    }

    @Override
    public void visit(Upsert upsert) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(UseStatement use) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Block block) {
        // TODO Auto-generated method stub
        
    }

}
