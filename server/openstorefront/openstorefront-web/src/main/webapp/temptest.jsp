<%-- 
    Document   : temptest.jsp
    Created on : Apr 23, 2018, 1:44:52 PM
    Author     : dshurtleff
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<stripes:layout-render name="layout/toplevelLayout.jsp">
    <stripes:layout-component name="contents">		
	
		
		<script type="text/javascript">
			/* global Ext, CoreUtil */
			
			Ext.require('OSF.customSubmission.SubmissionFormFullControl');
			
			Ext.onReady(function() {
				
				var subForm = Ext.create('OSF.customSubmission.SubmissionFormFullControl', {
				});
								
				var viewport = Ext.create('Ext.container.Viewport', {
					layout: 'fit',
					items: [
						subForm
					]
				});
				
				Ext.Ajax.request({
					url: 'testtemplateAct.json',
					success: function(response, opts) {
						var template = Ext.decode(response.responseText);
						subForm.load(template, 'test');
					}
				});				
			
				
			});			
		</script>			
		
	</stripes:layout-component>
</stripes:layout-render>				
			