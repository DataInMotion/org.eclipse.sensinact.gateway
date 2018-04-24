package org.eclipse.sensinact.gateway.nthbnd.endpoint.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.StateVariableResource;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.AccessTreeImpl;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.DefaultNorthboundRequestHandler;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRecipient;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequest;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestBuilder;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestHandler;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 *
 */
public class NorthboundRequestHandlerTest {
	
	protected TestContext testContext;
	protected Dictionary<String,Object> props;
	protected AccessTree tree;
	

	private final BundleContext context = Mockito.mock(BundleContext.class);
	private final Bundle bundle = Mockito.mock(Bundle.class);

	@Before
	public void init() 
			throws InvalidServiceProviderException, InvalidSyntaxException, 
			SecuredAccessException, BundleException 
	{	    
		this.testContext = new TestContext();
		this.tree = new AccessTreeImpl(testContext.getMediator()
			).withAccessProfile(AccessProfileOption.ALL_ANONYMOUS);
    }
	
    
	@After
	public void tearDown()
	{
		this.testContext.stop();
	}
	
    @Test
    public void testFilteringAvailable() throws Throwable
    {
    	ServiceImpl service =  
    		this.testContext.getModelInstance().getRootElement(
    			).addService("testService");

    	ResourceImpl r1impl = service.addActionResource(
    			"TestAction", ActionResource.class); 	
    	
    	ResourceImpl r2impl = service.addDataResource(
    			StateVariableResource.class, "TestVariable", 
    			String.class, "untriggered"); 	
    	
    	Thread.sleep(1000);

    	NorthboundRequestHandler handler = new DefaultNorthboundRequestHandler();
    	NorthboundRequestWrapper wrapper = getRequestWrapper("/xfilter:yfilter:sensinact",null, Arrays.asList("a") ,
    			Arrays.asList("a"));
    	handler.init(wrapper);
    	assertTrue(handler.processRequestURI());
    	NorthboundRequestBuilder builder = handler.handle();
    	NorthboundRequest request = builder.build();
    	NorthboundEndpoint endpoint = new NorthboundEndpoint(
    			(NorthboundMediator) testContext.getMediator(), null);
    	AccessMethodResponse response = endpoint.execute(request);
    	
    	String obj = response.getJSON();
    	System.out.println(obj);

    	JSONAssert.assertEquals(new JSONObject("{\"providers\":"
		+ "[{\"locYtion\":\"45.19334890078532:5.706474781036377\","
		+ "\"services\":[{\"resources\":"
        + "[{\"type\":\"PROPERTY\",\"nYme\":\"friendlyNYme\"},"
        + "{\"type\":\"PROPERTY\",\"nYme\":\"locYtion\"},"
        + "{\"type\":\"PROPERTY\",\"nYme\":\"bridge\"},"
        + "{\"type\":\"PROPERTY\",\"nYme\":\"icon\"}],"
        + "\"nYme\":\"Ydmin\"},"
        + "{\"resources\":"
        + "[{\"type\":\"ACTION\",\"nYme\":\"TestAction\"},"
        + "{\"type\":\"STATE_VARIABLE\",\"nYme\":\"TestVYriYble\"}],"
        + "\"nYme\":\"testService\"}],\"nYme\":\"serviceProvider\"}],"
        + "\"filters\":[{\"definition\":\"a\",\"type\":\"xfilter\"},"
    	  + "{\"definition\":\"a\",\"type\":\"yfilter\"}]"
        + ",\"statusCode\":200,\"type\":\"COMPLETE_LIST\"}") ,
    	new JSONObject(obj), false);
    	
    	wrapper = getRequestWrapper("/yfilter:xfilter:sensinact",null, Arrays.asList("a"),Arrays.asList("a"));
    	handler.init(wrapper);
    	assertTrue(handler.processRequestURI());
    	builder = handler.handle();
    	request = builder.build();
    	response = endpoint.execute(request);
    	
    	obj = response.getJSON();
    	System.out.println(obj);
    	
    	JSONAssert.assertEquals(new JSONObject("{\"providers\":"
		+ "[{\"locXtion\":\"45.19334890078532:5.706474781036377\","
		+ "\"services\":[{\"resources\":"
        + "[{\"type\":\"PROPERTY\",\"nXme\":\"friendlyNXme\"},"
        + "{\"type\":\"PROPERTY\",\"nXme\":\"locXtion\"},"
        + "{\"type\":\"PROPERTY\",\"nXme\":\"bridge\"},"
        + "{\"type\":\"PROPERTY\",\"nXme\":\"icon\"}],"
        + "\"nXme\":\"Xdmin\"},"
        + "{\"resources\":"
        + "[{\"type\":\"ACTION\",\"nXme\":\"TestAction\"},"
        + "{\"type\":\"STATE_VARIABLE\",\"nXme\":\"TestVXriXble\"}],"
        + "\"nXme\":\"testService\"}],\"nXme\":\"serviceProvider\"}],"
        + "\"filters\":[{\"definition\":\"a\",\"type\":\"xfilter\"},"
    	  + "{\"definition\":\"a\",\"type\":\"yfilter\"}]"
        + ",\"statusCode\":200,\"type\":\"COMPLETE_LIST\"}") ,
    	new JSONObject(obj), false);
    	
    	wrapper = getRequestWrapper("/yfilter:xfilter:sensinact",null, Arrays.asList("a"),Arrays.asList("f"));
    	handler.init(wrapper);
    	assertTrue(handler.processRequestURI());
    	builder = handler.handle();
    	request = builder.build();
    	response = endpoint.execute(request);
    	
    	obj = response.getJSON();
    	System.out.println(obj);
    	
    	JSONAssert.assertEquals(new JSONObject("{\"providers\":"
		+ "[{\"locXtion\":\"45.19334890078532:5.706474781036377\","
		+ "\"services\":[{\"resources\":"
        + "[{\"type\":\"PROPERTY\",\"nXme\":\"YriendlyNXme\"},"
        + "{\"type\":\"PROPERTY\",\"nXme\":\"locXtion\"},"
        + "{\"type\":\"PROPERTY\",\"nXme\":\"bridge\"},"
        + "{\"type\":\"PROPERTY\",\"nXme\":\"icon\"}],"
        + "\"nXme\":\"Xdmin\"},"
        + "{\"resources\":"
        + "[{\"type\":\"ACTION\",\"nXme\":\"TestAction\"},"
        + "{\"type\":\"STATE_VARIABLE\",\"nXme\":\"TestVXriXble\"}],"
        + "\"nXme\":\"testService\"}],\"nXme\":\"serviceProvider\"}],"
        + "\"filters\":[{\"definition\":\"a\",\"type\":\"xfilter\"},"
    	  + "{\"definition\":\"f\",\"type\":\"yfilter\"}]"
        + ",\"statusCode\":200,\"type\":\"COMPLETE_LIST\"}") ,
    	new JSONObject(obj), false);
    }

    @Test
    public void testFilteringUnavailable() throws Throwable
    {
    	ServiceImpl service =  
    		this.testContext.getModelInstance().getRootElement(
    			).addService("testService");

    	ResourceImpl r1impl = service.addActionResource(
    			"TestAction", ActionResource.class); 	
    	
    	ResourceImpl r2impl = service.addDataResource(
    			StateVariableResource.class, "TestVariable", 
    			String.class, "untriggered"); 	
    	
    	Thread.sleep(1000);
    	testContext.setXFilterAvailable(false);
    	testContext.setYFilterAvailable(false);
    	
    	NorthboundRequestHandler handler = new DefaultNorthboundRequestHandler();
    	NorthboundRequestWrapper wrapper = getRequestWrapper("/xfilter:yfilter:sensinact",null, Arrays.asList("a") ,
    			Arrays.asList("a"));
    	handler.init(wrapper);
    	assertTrue(handler.processRequestURI());
    	NorthboundRequestBuilder builder = handler.handle();
    	NorthboundRequest request = builder.build();
    	NorthboundEndpoint endpoint = new NorthboundEndpoint(
    			(NorthboundMediator) testContext.getMediator(), null);
    	AccessMethodResponse response = endpoint.execute(request);
    	
    	String obj = response.getJSON();
    	System.out.println(obj);

    	JSONAssert.assertEquals(new JSONObject("{\"providers\":"
		+ "[{\"location\":\"45.19334890078532:5.706474781036377\","
		+ "\"services\":[{\"resources\":"
        + "[{\"type\":\"PROPERTY\",\"name\":\"friendlyName\"},"
        + "{\"type\":\"PROPERTY\",\"name\":\"location\"},"
        + "{\"type\":\"PROPERTY\",\"name\":\"bridge\"},"
        + "{\"type\":\"PROPERTY\",\"name\":\"icon\"}],"
        + "\"name\":\"admin\"},"
        + "{\"resources\":"
        + "[{\"type\":\"ACTION\",\"name\":\"TestAction\"},"
        + "{\"type\":\"STATE_VARIABLE\",\"name\":\"TestVariable\"}],"
        + "\"name\":\"testService\"}],\"name\":\"serviceProvider\"}],"  
       // + "\"filters\":[{\"definition\":\"a\",\"type\":\"xfilter\"},"
       // + "{\"definition\":\"f\",\"type\":\"yfilter\"}],"
        + "\"statusCode\":200,\"type\":\"COMPLETE_LIST\"}") ,
    	new JSONObject(obj), false);
    	
    	testContext.setYFilterAvailable(true);
    	wrapper = getRequestWrapper("/yfilter:xfilter:sensinact",null, Arrays.asList("a"),Arrays.asList("f"));
    	handler.init(wrapper);
    	assertTrue(handler.processRequestURI());
    	builder = handler.handle();
    	request = builder.build();
    	response = endpoint.execute(request);
    	
    	obj = response.getJSON();
    	System.out.println(obj);
    	
    	JSONAssert.assertEquals(new JSONObject("{\"providers\":"
			+ "[{\"location\":\"45.19334890078532:5.706474781036377\","
			+ "\"services\":[{\"resources\":"
	        + "[{\"type\":\"PROPERTY\",\"name\":\"YriendlyName\"},"
	        + "{\"type\":\"PROPERTY\",\"name\":\"location\"},"
	        + "{\"type\":\"PROPERTY\",\"name\":\"bridge\"},"
	        + "{\"type\":\"PROPERTY\",\"name\":\"icon\"}],"
	        + "\"name\":\"admin\"},"
	        + "{\"resources\":"
	        + "[{\"type\":\"ACTION\",\"name\":\"TestAction\"},"
	        + "{\"type\":\"STATE_VARIABLE\",\"name\":\"TestVariable\"}],"
	        + "\"name\":\"testService\"}],\"name\":\"serviceProvider\"}],"
	        + "\"filters\":[{\"definition\":\"f\",\"type\":\"yfilter\"}]"
	        + ",\"statusCode\":200,\"type\":\"COMPLETE_LIST\"}") ,
	    	new JSONObject(obj), false);

    	testContext.setYFilterAvailable(false);
    	testContext.setXFilterAvailable(true);
    	
    	wrapper = getRequestWrapper("/yfilter:xfilter:sensinact",null, Arrays.asList("a"),Arrays.asList("f"));
    	handler.init(wrapper);
    	assertTrue(handler.processRequestURI());
    	builder = handler.handle();
    	request = builder.build();
    	response = endpoint.execute(request);
    	
    	obj = response.getJSON();
    	System.out.println(obj);
    	
    	JSONAssert.assertEquals(new JSONObject("{\"providers\":"
		+ "[{\"locXtion\":\"45.19334890078532:5.706474781036377\","
		+ "\"services\":[{\"resources\":"
        + "[{\"type\":\"PROPERTY\",\"nXme\":\"friendlyNXme\"},"
        + "{\"type\":\"PROPERTY\",\"nXme\":\"locXtion\"},"
        + "{\"type\":\"PROPERTY\",\"nXme\":\"bridge\"},"
        + "{\"type\":\"PROPERTY\",\"nXme\":\"icon\"}],"
        + "\"nXme\":\"Xdmin\"},"
        + "{\"resources\":"
        + "[{\"type\":\"ACTION\",\"nXme\":\"TestAction\"},"
        + "{\"type\":\"STATE_VARIABLE\",\"nXme\":\"TestVXriXble\"}],"
        + "\"nXme\":\"testService\"}],\"nXme\":\"serviceProvider\"}],"
        + "\"filters\":[{\"definition\":\"a\",\"type\":\"xfilter\"}]"
        + ",\"statusCode\":200,\"type\":\"COMPLETE_LIST\"}") ,
    	new JSONObject(obj), false);
    }

	private final NorthboundRequestWrapper getRequestWrapper(
			final String uri, final String requestId,
			final List<String> xfilter, final List<String> yfilter)
	{
		return new NorthboundRequestWrapper(){

			@Override
			public NorthboundMediator getMediator() {
				return (NorthboundMediator) testContext.getMediator();
			}

			@Override
			public String getRequestURI() {				
				return uri;
			}

			@Override
			public String getRequestID(Parameter[] parameters) {				
				return requestId;
			}

			@Override
			public Map<String, List<String>> getQueryMap() {				
				return new HashMap<String,List<String>>(){{
					put("xfilter",xfilter);
					put("yfilter",yfilter);
					/*put("hideFilter",Arrays.asList("true"));	*/				
				}};
			}

			@Override
			public String getContent() {				
				return null;
			}

			@Override
			public NorthboundRecipient createRecipient(Parameter[] parameters) {				
				return null;
			}

			@Override
			public Authentication<?> getAuthentication() {				
				return null;
			}
		};
	}
}