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

    public void printGreeting( final String message )
    {
        try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    StatementResult result = tx.run( "CREATE (a:Greeting) " +
                                                     "SET a.message = $message " +
                                                     "RETURN a.message + ', from node ' + id(a)",
                            parameters( "message", message ) );
                    return result.single().get( 0 ).asString();
                }
            } );
            System.out.println( greeting );
        }
    }
    
    public static void main(String args[]) {
        // to connect to RRes test neo4j bolt server (url, username, password)
        try ( testMain greeter = new testMain( "bolt://babvs48.rothamsted.ac.uk:7688", "neo4j", "test" ) )
        {
            greeter.printGreeting( "Connected..., msg: " );
        }
        catch(Exception ex)
            {
                ex.printStackTrace();
            }
        
    }
}
