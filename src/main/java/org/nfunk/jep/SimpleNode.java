/*****************************************************************************
 * JEP - Java Math Expression Parser 2.3.1
 * January 26 2006
 * (c) Copyright 2004, Nathan Funk and Richard Morris
 * See LICENSE.txt for license information.
 *****************************************************************************/
/* Generated By:JJTree: Do not edit this line. SimpleNode.java */
package org.nfunk.jep;

public class SimpleNode implements Node
{
    protected Node   parent;
    protected Node[] children;
    protected int    id;
    protected Parser parser;

    public SimpleNode(int i)
    {
        this.id = i;
    }

    public SimpleNode(Parser p, int i)
    {
        this(i);
        this.parser = p;
    }

    /** Accept the visitor. **/
    public Object childrenAccept(ParserVisitor visitor, Object data) throws ParseException
    {
        if (this.children != null) for (final Node element : this.children)
            element.jjtAccept(visitor, data);
        return data;
    }

    public void dump(String prefix)
    {
        System.out.println(this.toString(prefix));
        if (this.children != null) for (final Node element : this.children)
        {
            final SimpleNode n = (SimpleNode) element;
            if (n != null) n.dump(prefix + " ");
        }
    }

    /**
     * Returns the id of the node (for simpler identification).
     */
    public int getId()
    {
        return this.id;
    }

    /** Accept the visitor. **/
    @Override
    public Object jjtAccept(ParserVisitor visitor, Object data) throws ParseException
    {
        return visitor.visit(this, data);
    }

    @Override
    public void jjtAddChild(Node n, int i)
    {
        if (this.children == null) this.children = new Node[i + 1];
        else if (i >= this.children.length)
        {
            final Node c[] = new Node[i + 1];
            System.arraycopy(this.children, 0, c, 0, this.children.length);
            this.children = c;
        }
        this.children[i] = n;
    }

    @Override
    public void jjtClose()
    {
    }

    @Override
    public Node jjtGetChild(int i)
    {
        return this.children[i];
    }

    @Override
    public int jjtGetNumChildren()
    {
        return this.children == null ? 0 : this.children.length;
    }

    @Override
    public Node jjtGetParent()
    {
        return this.parent;
    }

    /*
     * You can override these two methods in subclasses of SimpleNode to
     * customize the way the node appears when the tree is dumped. If
     * your output uses more than one line you should override
     * toString(String), otherwise overriding toString() is probably all
     * you need to do.
     */

    @Override
    public void jjtOpen()
    {
    }

    @Override
    public void jjtSetParent(Node n)
    {
        this.parent = n;
    }

    /*
     * Override this method if you want to customize how the node dumps
     * out its children.
     */

    @Override
    public String toString()
    {
        return ParserTreeConstants.jjtNodeName[this.id];
    }

    public String toString(String prefix)
    {
        return prefix + this.toString();
    }
}
