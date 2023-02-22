package org.uth;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;

@Path("/endpoints")
public class BootCamp
{
  private boolean _ignoreState = false;
  private long _start = System.currentTimeMillis();

  @Path("health")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String health() throws InterruptedException 
  {
    if( !_ignoreState )
    {
      long elapsed = System.currentTimeMillis() - _start;

      long diff = Math.round( elapsed / 1000 );

      return "Elapsed " + diff + " seconds";
    }
    else
    {
      throw new BadRequestException();
    }

//    } else {
//      System.out.println( "Ignoring the health probe");
//      Thread.sleep(20000);
//    }

  }

  @Path("envVars")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String envVars()
  {
      String var1 = System.getenv("VAR1");
      String var2 = System.getenv("VAR2");
      String ipInformation = null;
      String returnMessage = "";

      try
      {
        InetAddress localAddress = InetAddress.getLocalHost();
        ipInformation = localAddress.toString();
      }
      catch( UnknownHostException exc )
      {
        ipInformation = "(Unknown Host Exception in App)";
      }
      catch( Exception exc )
      {
        ipInformation = "(Other exception in App: " + exc.toString() + ")";
      }

      if ((var1 != null) && (var1.length() > 0)) {
          System.out.println( "ENV found - var1: " + var1);
          returnMessage = ipInformation + "\nEnvironment variable : VAR1 --> " + var1;
      }

      if ((var2 != null) && (var2.length() > 0)) {
          System.out.println( "ENV found - var2: " + var2);
          if (returnMessage.length() > 0) {
            returnMessage += "\n";
          } else {
            returnMessage = ipInformation + "\n";
          }
          returnMessage += "Environment variable : VAR2 --> " + var2;
      }
      if (returnMessage == "") {
        returnMessage = ipInformation + " No environment variables have been set";
      }

      returnMessage += "\n";

    return returnMessage;
  }

  @Path("setIgnoreState")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String setIgnoreState(@QueryParam("state") boolean state )
  {
    _ignoreState = state;

    return "Ignore state set to " + state;
  }

  @Path("callLayers")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String callLayers()
  {
    // Requires an ENV variable for nextLayer (NEXTLAYER)
    String nextLayer = System.getenv("NEXTLAYER");

    // (Log)
    System.out.println( "ENV found: " + nextLayer );

    String ipInformation = null;

    try
    {
      InetAddress localAddress = InetAddress.getLocalHost();
      ipInformation = localAddress.toString();
    }
    catch( UnknownHostException exc )
    {
      ipInformation = "(Unknown Host Exception in App)";
    }
    catch( Exception exc )
    {
      ipInformation = "(Other exception in App: " + exc.toString() + ")";
    }

    // If there is no ENV variable, just return the current IP details
    if( nextLayer == null )
    {
      return ipInformation;
    }
    // Otherwise call on to the next layer and add that information to the return
    else
    {
      // Security fix for certs
      fixSecurity();

      System.out.println( "Fetching " + nextLayer + "/endpoints/callLayers" );

      String targetURL = nextLayer + "/endpoints/callLayers";

      try
      {
        URL url = new URL( targetURL );
        HttpsURLConnection getConnection = (HttpsURLConnection)url.openConnection();

        getConnection.setRequestMethod( "GET" );
        getConnection.setRequestProperty( "Content-Type", "text/plain" );
        getConnection.setDoOutput(true);

        int responseCode = getConnection.getResponseCode();

        System.out.println( "Response: " + responseCode );

        // If it's a valid connection, pull the info
        if( responseCode == 200 )
        {
          BufferedReader in = new BufferedReader( new InputStreamReader( getConnection.getInputStream()));
          String inputLine = null;
          StringBuffer content = new StringBuffer();

          while(( inputLine = in.readLine()) != null )
          {
            content.append( inputLine );
          }

          in.close();

          System.out.println( "Received: " + content.toString() );

          ipInformation = ipInformation + " " + content.toString();
        }
        else
        {
          ipInformation = ipInformation + " " + "(Unreachable " + targetURL + ")";
        }
      }
      catch( Exception exc )
      {
        ipInformation = ipInformation + " " + "(Exception occurred connecting to " + targetURL + " " + exc.toString() + ")";
      }

      return ipInformation + "\n";
    }
  }

  // Unfortunately need to add a security fix for https due to the nature of the certs in the demo
  // NOT FOR PRODUCTION
  private void fixSecurity()
  {
    TrustManager[] trustAllCerts = new TrustManager[]
    {
      new X509TrustManager()
      {
        public java.security.cert.X509Certificate[] getAcceptedIssuers()
        {
          return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {  }
     }
    };

    try
    {
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

      // Create all-trusting host name verifier
      HostnameVerifier allHostsValid = new HostnameVerifier()
      {
        public boolean verify(String hostname, SSLSession session)
        {
          return true;
        }
      };

      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
    catch( Exception exc )
    {
      System.out.println( "Unable to override security due to " + exc.toString() );
    }
  }
}
