package rres.neo4j_test;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import static org.neo4j.driver.v1.Values.parameters;

/**
 *
 * @author singha
 */
public class testMain implements AutoCloseable {

    private final Driver driver;

    public testMain( String uri, String user, String password )
    {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

    @Override
    public void close() throws Exception
    {
        driver.close();
    }

    public void printGreeting(final String message)
    {
        try ( Session session = driver.session() )
        {
            String connection_greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    StatementResult result = tx.run( "CREATE (a:Greeting) " +
                                                     "SET a.message = $message " +
                                                     "RETURN a.message + ', greetings from neo4j node: ' + id(a)",
                            parameters( "message", message ) );
                    return result.single().get( 0 ).asString();
                }
            } );
            System.out.println(connection_greeting);
        }
    }
    
    public static void main(String args[]) {
        // to connect to RRes test neo4j bolt server (url, username, password)
        try {
            testMain tm= new testMain( "bolt://babvs48.rothamsted.ac.uk:7688", "neo4j", "test" );
            tm.printGreeting("Connected...");
            
            // some simple test queries.
            tm.testQueries();
        }
        catch(Exception ex)
            {
                ex.printStackTrace();
            }
        
    }

    public void testQueries() {
        try ( Session session = driver.session() )
        {
            String query_results= session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    StatementResult count_nodes = tx.run("MATCH (n) RETURN count(*)");
                    StatementResult count_edges = tx.run("MATCH ()-[r]->() RETURN count(r)");
                    StatementResult wheat_genes = tx.run("MATCH p=( g:Gene {TAXID:\"4565\"} ) RETURN count(p)");
                    
                    return "Total nodes= "+ count_nodes.single().get(0).asInt() 
                            +", edges= "+ count_edges.single().get(0).asInt() 
                            +", \n Wheat genes: "+ wheat_genes.single().get(0).asInt(); // use .asString() for String results
                }
            } );
            System.out.println(query_results);
        }
    }
}
