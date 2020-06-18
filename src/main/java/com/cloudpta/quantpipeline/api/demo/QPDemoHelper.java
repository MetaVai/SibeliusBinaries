////////////////////////////////////////////////////////////////////////////////
//
//                                 NOTICE:
//  THIS PROGRAM CONSISTS OF TRADE SECRECTS THAT ARE THE PROPERTY OF
//  Advanced Products Ltd. THE CONTENTS MAY NOT BE USED OR DISCLOSED
//  WITHOUT THE EXPRESS WRITTEN PERMISSION OF THE OWNER.
//
//               COPYRIGHT Advanced Products Ltd 2016-2019
//
////////////////////////////////////////////////////////////////////////////////
package com.cloudpta.quantpipeline.api.demo;

import akillesinc.data.SimpleJSONDataSource;
import akillesinc.execution.SimpleGBlackScholesExecutor;
import akillesinc.pricer.Pricer;
import akillesinc.recipes.SimpleJSONMeasure;
import akillesinc.recipes.SimpleJSONRecipe;
import akillesinc.utils.AbstractData;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.json.JsonObject;

/**
 *
 * @author Danny
 */
public class QPDemoHelper
{
    protected String getDateInRightFormat(String qpFormat)
    {
        return null;
    }
    
    public String processData(String inputData)
    {
        JsonObject requestAsJson = null;
        // Turn into json
        String maturityDateInQPFormat = requestAsJson.getString("maturity");
        String maturityDate = getDateInRightFormat(maturityDateInQPFormat);
        String calculationDateInQPFormat = requestAsJson.getString("price_date");
        String calculationDate = getDateInRightFormat(calculationDateInQPFormat);
        String spotPrice = requestAsJson.getString("spot_price");
        String strikePrice = requestAsJson.getString("strike_price");
        String volatility = requestAsJson.getString("volatility");
        String riskFreeRate = requestAsJson.getString("rate");
        
        // Turn it into a request string for gershwin
        // format is option(stock=.HSI,strike=140,putCall=true,maturity=2021-05-06,b=0.001,T=2020-05-04,S=127.62,r=0.001,sigma=0.20)
        String gershwinPriceRequestString = "option(stock=.HSI,strike=";
        gershwinPriceRequestString = gershwinPriceRequestString + strikePrice;
        gershwinPriceRequestString = gershwinPriceRequestString + ",putCall=true,maturity=";
        gershwinPriceRequestString = gershwinPriceRequestString + maturityDate;
        gershwinPriceRequestString = gershwinPriceRequestString + ",b=";
        gershwinPriceRequestString = gershwinPriceRequestString + riskFreeRate;
        gershwinPriceRequestString = gershwinPriceRequestString + ",T=";
        gershwinPriceRequestString = gershwinPriceRequestString + calculationDate;
        gershwinPriceRequestString = gershwinPriceRequestString + ",S=";
        gershwinPriceRequestString = gershwinPriceRequestString + spotPrice;
        gershwinPriceRequestString = gershwinPriceRequestString + ",r=";
        gershwinPriceRequestString = gershwinPriceRequestString + riskFreeRate;
        gershwinPriceRequestString = gershwinPriceRequestString + ",sigma=";
        gershwinPriceRequestString = gershwinPriceRequestString + volatility;
        gershwinPriceRequestString = gershwinPriceRequestString + ")";
        
        return gershwinPriceRequestString;
    }
    
    public double getPrice(String requestString)
    {
        double price = -1;
        
        // Load up the pricer
        SimpleGBlackScholesExecutor.touch();
        SimpleJSONDataSource.touch();
        SimpleJSONMeasure.touch();
        SimpleJSONRecipe.touch();

        // load the form for pricing
        InputStream inputStream = new ByteArrayInputStream(engineParameters.getBytes());
        var re = AbstractData.loadJSON(inputStream);
        var pricer = Pricer.Factory(re);
       
        try 
        {
            var rr = pricer.price(requestString);
            for (var x: rr.entries) 
            {
                var labels = "";
                for (var k: x.categories) 
                {
                    labels = labels + k + "\t";
                }
                var risks = "";
                for (var r: x.risk[0]) 
                {
                    risks = risks + r + "\t";
                }
                System.out.println(x.label + " " + x.riskName + " pnl impact=" + x.pnlContributions[0][0]);
                System.out.println(labels);
                System.out.println(risks);
            }

            // price @ T
            price = rr.report.parameters.get("update").toDouble();
        } 
        catch(Throwable e) 
        {
            e.printStackTrace();
            throw e;
        }
        
        return price;
    }
    
    static String engineParameters = "{\n" +
"        \"inputs\": {\n" +
"            \"type\": \"Parameters\",\n" +
"            \"value\": {\n" +
"                \"stock\": \"string\",\n" +
"                \"strike\": \"double\",\n" +
"                \"putCall\": \"bool\",\n" +
"                \"maturity\": \"date\",\n" +
"                \"T\": \"date\",\n" +
"                \"S\": \"double\",\n" +
"                \"r\": \"double\",\n" +
"                \"b\": \"double\",\n" +
"                \"sigma\": \"double\"\n" +
"            }\n" +
"        },\n" +
"        \"riskEngineRecipe\":\n" +
"        {\n" +
"            \"type\": \"Parameters\",\n" +
"            \"value\": {\n" +
"                \"category\": \"SimpleJSONRecipe\",\n" +
"                \"pattern\": {\n" +
"                    \"#quote\" : {\n" +
"                        \"executor\": {\n" +
"                            \"type\": \"Parameters\",\n" +
"                            \"value\": {\n" +
"                                \"category\": \"SimpleGBlackScholesExecutor\"\n" +
"                            }\n" +
"                        },\n" +
"                        \"dataSource\" : {\n" +
"                            \"type\": \"Parameters\",\n" +
"                            \"value\": {\n" +
"                                \"category\": \"SimpleJSONDataSource\",\n" +
"                                \"hasDate\": true,\n" +
"                                \"hasCategory\": true,\n" +
"                                \"json\": {}\n" +
"                            }\n" +
"                        },\n" +
"                        \"recipe\": {\n" +
"                            \"type\": \"Parameters\",\n" +
"                            \"value\": {\n" +
"                                \"category\": \"SimpleJSONRecipe\",\n" +
"                                \"pattern\": {\n" +
"                                    \"market\": [\"#unquote\", {\n" +
"                                        \"date\": [\"#shortDate\", \"[T]\"],\n" +
"                                        \"S\": [\"#double\",\"[S]\"],\n" +
"                                        \"r\": [\"#double\",\"[r]\"],\n" +
"                                        \"b\": [\"#double\",\"[b]\"],\n" +
"                                        \"sigma\": [\"#double\",\"[sigma]\"]\n" +
"                                    }],\n" +
"                                    \"refMarket\": [\"#unquote\", {\n" +
"                                        \"date\": [\"#shortDate\", \"[T]\"],\n" +
"                                        \"S\": [\"#double\",\"[S]\"],\n" +
"                                        \"r\": [\"#double\",\"[r]\"],\n" +
"                                        \"b\": [\"#double\",\"[b]\"],\n" +
"                                        \"sigma\": [\"#double\",\"[sigma]\"]\n" +
"                                    }],\n" +
"                                    \"option\": {\n" +
"                                        \"stock\": [\"#unquote\", \"[stock]\"],\n" +
"                                        \"strike\": [\"#unquote\", [\"#double\", \"[strike]\"]],\n" +
"                                        \"putCall\": [\"#unquote\", [\"#bool\", \"[putCall]\"]],\n" +
"                                        \"maturity\": [\"#unquote\", [\"#shortDate\", \"[maturity]\"]]\n" +
"                                    }\n" +
"                                }\n" +
"                            }\n" +
"                        },\n" +
"                        \"comparand\": {\n" +
"                            \"type\": \"Parameters\",\n" +
"                            \"value\": {\n" +
"                                \"category\": \"SimpleJSONRecipe\",\n" +
"                                \"pattern\": {\n" +
"                                    \"market\": [\"#unquote\", {\n" +
"                                        \"date\": [\"#shortDate\", \"[T]\"],\n" +
"                                        \"S\": [\"#double\", \"[S]\"],\n" +
"                                        \"r\": [\"#double\",\"[r]\"],\n" +
"                                        \"b\": [\"#double\",\"[b]\"],\n" +
"                                        \"sigma\": [\"#double\",\"[sigma]\"]\n" +
"                                    }],\n" +
"                                    \"refMarket\": [\"#unquote\", {\n" +
"                                        \"date\": [\"#shortDate\", \"[T]\"],\n" +
"                                        \"S\": [\"#double\",\"[S]\"],\n" +
"                                        \"r\": [\"#double\",\"[r]\"],\n" +
"                                        \"b\": [\"#double\",\"[b]\"],\n" +
"                                        \"sigma\": [\"#double\",\"[sigma]\"]\n" +
"                                    }],\n" +
"                                    \"option\": {\n" +
"                                        \"stock\": [\"#unquote\", \"[stock]\"],\n" +
"                                        \"strike\": [\"#unquote\", [\"#double\", \"[strike]\"]],\n" +
"                                        \"putCall\": [\"#unquote\", [\"#bool\", \"[putCall]\"]],\n" +
"                                        \"maturity\": [\"#unquote\", [\"#shortDate\", \"[maturity]\"]]\n" +
"                                    }\n" +
"                                }\n" +
"                            }\n" +
"                        },\n" +
"                        \"risks\": {\n" +
"                            \"type\": \"Parameters[]\",\n" +
"                            \"value\": []   \n" +
"                        },\n" +
"                        \"labels\":\"Artaxerces\"\n" +
"                    }\n" +
"                }\n" +
"            }\n" +
"        }\n" +
"}";
}
