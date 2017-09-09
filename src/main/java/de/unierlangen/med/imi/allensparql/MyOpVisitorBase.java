/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.unierlangen.med.imi.allensparql;

import java.util.List;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.OpWalker;
import org.apache.jena.sparql.algebra.op.OpBGP;

/**
 *
 * @author matesn
 */
public class MyOpVisitorBase extends OpVisitorBase
{
    public void myOpVisitorWalker(Op op)
    {
        OpWalker.walk(op, this);
    }

    @Override
    public void visit(final OpBGP opBGP) {
        final List<Triple> triples = opBGP.getPattern().getList();
        int i = 0;
        for (final Triple triple : triples) {
            System.out.println("Triple: "+triple.toString());
        }
    }
}