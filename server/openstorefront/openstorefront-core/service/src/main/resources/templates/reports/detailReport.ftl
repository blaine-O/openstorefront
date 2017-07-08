<#ftl strip_whitespace = true>

<head>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<style>
		body {
			font-family: Helvetica, Verdana, Arial, sans-serif; 
		}
		h1 { 
			background-color: #F1F1F1;
			padding: 5px; 
		} 
		tr:nth-child(odd) { 
			background-color: #eeeeee 
		} 
		tr:nth-child(even) {  
			background-color: white; 
		} 
		@media print {
			.pageBreak { 
				background-color: #F1F1F1;
			}
		} 
		table { 
			border: 1px black solid; 
			border-collapse: collapse;
			border-spacing: 0;
			width: 100%;
		} 
		table td,th { 
			padding-left: 5px; 
			padding-right: 5px; 
		} 
		th { 
			color: white; 
			background-color: #414e68; 
			border: 1px lightgray solid; 
		} 
		td { 
			border: 1px lightgray solid;
			padding: 5px;
		}
		tr:nthchild(odd) {
			backgroundcolor: #eeeeee;
		}
		tr:nthchild(even) {
			backgroundcolor: white;
		}
		.eval-version {
			color: #848484;
			font-size: 0.75em;
		}
		.eval-header {
			margin-bottom: 0px;
		}
		.evaluation-section {
			margin-left: 3%;
			width: 97%;
		}
		@media print {
			.pageBreak {
				page-break-after: always; 
			}
		}
		.rolling-container {
			width: 100%;        
		}
		.detail-eval-item {    
			padding: 5px;
			border: 1px solid grey;
			min-width: 311px;
			max-width: 100%;
			width: 16.7%;
			border-collapse: collapse;
			display: inline-block;
			width: 32%;
			height: 2em;
		}
		.detail-eval-item:hover  { 
			background-color: #f5f5f5;
		}
		.rolling-container-block {    
			float: left;		
		}
		.score-circle {
			display: inline-block;
			color: black;
			font-style: normal;
			letter-spacing: -5px;
		}
		.score-average {
			clear: both;
			float: right;
			margin-right: 11px;
			font-size: 0.75em;
		}
		.detail-eval-score {
			float: right;
			margin-right: 15px;
		}
	</style>
</head>
<body>
	<div>
		<p>Component Details Report: ${reportDate}</p>
		<p>Entries (${reportSize})</p>
	</div>
	<hr />
	<#list components as component>
	
		<!--Organization Description-->
		<#if reportOptions.getDisplayOrgData() == true>
			<h1>${component.component.getName()}</h1>
			<p><b>${component.component.getOrganization()}</b></p>
			<br>
			<br>
		</#if>
		<div>
			<#if allowSecurityMargkingsFlg == true>
				${component.component.getSecurityMarkingType()}
			</#if>
		</div>
		
		<!--Description-->
		<#if reportOptions.getDisplayDescription()>
			<div>
				${component.component.getDescription()}
			</div>
		</#if>
		
		<!--Vitals-->
		<#if component.vitals?has_content && reportOptions.getDisplayVitals() == true>
			<h2>Vitals</h2>
			<table>
				<tr>
					<th>Vital</th>
					<th>Value</th>
				</tr>
				<#list component.vitals as vitals>
					<tr>
						<td>
							<b>${vitals.typeLabel}</b>
						</td>
						<td>
							${vitals.attributeLabel}
						</td>
					</tr>
				</#list>
			</table>
		</#if>
			
		<!--Meta Data-->
		<#if component.metaData?has_content && reportOptions.getDisplayMetaData() == true>
			<h2>MetaData</h2>
			<table>
				<tr>
					<th>Label</th>
					<th>Value</th>
				</tr>
				<#list component.metaData as metaData>
					<tr>
						<td><b>${metaData.label}</b></td>
						<td>${metaData.value}</td>
					</tr>
				</#list>
			</table>
		</#if>
			
		<!--Contacts-->
		<#if component.contacts?has_content && reportOptions.getDisplayContacts() == true>
			<h2>Contacts</h2>
			<table>
				<tr>
					<th>Type</th>
					<th>First Name</th>
					<th>Last Name</th>
					<th>Organization</th>
					<th>Email</th>
					<th>Phone</th>
				</tr>
				<#list component.contacts as contacts>
					<tr>
						<td><b>${contacts.type}</b></td>
						<td>${contacts.firstName}</td>
						<td>${contacts.lastName}</td>
						<td>${contacts.org}</td>
						<td>${contacts.email}</td>
						<td>${contacts.phone}</td>
					</tr>
				</#list>
			</table>
		</#if>
			
		<!--Resources-->
		<#if component.resources?has_content && reportOptions.getDisplayResources() == true>
			<h2>Resources</h2>
			<table>
				<tr>
					<th>Type</th>
					<th>Description</th>
					<th>Link</th>
					<th>Restricted (requires login/CAC)</th>
				</tr>
				<#list component.resources as resource>
					<tr>
						<td><b>${resource.type}</b></td>
						<td>${resource.description}</td>
						<td>${resource.link}</td>
						<td>${resource.restricted}</td>
					</tr>
				</#list>
			</table>
		</#if>
			
		<#if component.evaluations?has_content>
			<#assign flag = true>
			<#list component.evaluations as eval>
				<#if flag == true>
			
					<h2 class="eval-header">Evaluation</h2>
					<#if eval.version?has_content>
						<div class="eval-version">Version - ${eval.version}</div>
					</#if>
						
					<!--Evaluation Summary-->
					<#if reportOptions.getDisplayEvalSummary() == true || reportOptions.getDisplayEvalDetails() == true>
						<div class="evaluation-section">
							
							<!--Reusability Factors-->
							<#if eval.scores?has_content>
								<h3 class="eval-header">Reusability Factors (5 = best)</h3>
								<#assign scoreColumns = (eval.scores?size/10.0)?ceiling>
								<#list eval.scores as scoreItem>
									<div class="detail-eval-item">
										<span class="detail-eval-label">${scoreItem.factor} </span>
										<span class="detail-eval-score" data-qtip="${scoreItem.averageScore}">
											<#if scoreItem.score?is_number>
												<#list 2..scoreItem.score?number + 1 as ii>
													<i class="score-circle">&#1010${ii};</i>
												</#list>
											<#else>
												<b>N/A</b>
											</#if>
										</span>
										<div class="score-average">Average: ${scoreItem.averageScore}</div>
									</div>
								</#list>
							</#if>
							
							<!--Checklist summary-->
							<#if eval.checklistSummary?has_content>
								<h3 class="eval-header">Checklist Summary</h3>
								${eval.checklistSummary}
							</#if>

							<!--Checklist recommendations-->
							<#if eval.recommendations?has_content>
								<h3 class="eval-header">Evaluation Recommendations</h3>
								<table>
									<tr>
										<th>Type</th>
										<th>Recommendation</th>
										<th>Reason</th>
									</tr>
									<#list eval.recommendations as rec>
										<tr>
											<td style="width: 15%;">${rec.typeDescription}</td>
											<td style="width: 39%;">${rec.recommendation}</td>
											<td style="width: 45%;">${rec.reason}</td>
										</tr>
									</#list>
								</table>
							</#if>
						</div>
					</#if>
								
					<!--Evaluation Details-->
					<#if reportOptions.getDisplayEvalDetails() == true>
						<div class="evaluation-section">
							<!--Evaluation Sections-->
							<#if eval.evaluationSections?has_content>
								<#list eval.evaluationSections as section>
									<#if section.isPrivate == false>
										<h3 class="eval-header">${section.title}</h3>
										<#if section.hideContent == false>
											${section.content}
										</#if>

										<!--Sub Sections-->
										<#if section.subSections?has_content>
											<#list section.subSections as subSection>
												<#if subSection.isPrivate == false>
													<div class="evaluation-section">
														<#if subSection.hideTitle == false>
															<h3 class="eval-header">${subSection.title}</h3>
														</#if>
														<#if subSection.hideContent == false>
															${subSection.content}
														</#if>
													</div>
												</#if>
											</#list>
										</#if>
									</#if>
								</#list>
							</#if>
							
							<!--Checklist Details-->
							<#if eval.checklistDetails?has_content>
								<h3 class="eval-header">Evaluation Checklist Details</h3>
								<table>
									<tr>
										<th style="width: 2.5%;">QID</th>
										<th style="width: 5%;">Section</th>
										<th style="width: 45%;">Question</th>
										<th style="width: 2.5%;">Score</th>
										<th style="width: 45%;">Response</th>
									</tr>
									<#list eval.checklistDetails as detail>
										<tr>
											<td><#if detail.qId?has_content>${detail.qId}</#if></td>
											<td><#if detail.section?has_content>${detail.section}</#if></td>
											<td><#if detail.question?has_content>${detail.question}</#if></td>
											<td><#if detail.score?has_content>${detail.score}</#if></td>
											<td><#if detail.response?has_content>${detail.response}</#if></td>
										</tr>
									</#list>
								</table>
							</#if>
						</div>
					</#if>

					<#if reportOptions.getDisplayEvalVersions() != true>
						<#assign flag = false>
					</#if>
				</#if>
			</#list>
		</#if>
	</#list>
</body>
