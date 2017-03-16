/*
 * Copyright 2014 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usu.sdl.openstorefront.service.manager;

import edu.usu.sdl.openstorefront.common.manager.Initializable;
import edu.usu.sdl.openstorefront.common.manager.PropertiesManager;
import edu.usu.sdl.openstorefront.common.util.Convert;
import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.common.util.StringProcessor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.codemonkey.simplejavamail.email.Email;
import org.codemonkey.simplejavamail.email.Recipient;
import org.hazlewood.connor.bottema.emailaddress.EmailAddressValidator;

/**
 * Used for Handling Email
 *
 * @author dshurtleff
 */
public class MailManager
		implements Initializable
{

	private static final Logger log = Logger.getLogger(MailManager.class.getName());

	private static AtomicBoolean started = new AtomicBoolean(false);
	private static Mailer mailer;

	public static void init()
	{
		//pull properties
		String server = PropertiesManager.getValue(PropertiesManager.KEY_MAIL_SERVER);
		String serverPort = StringProcessor.nullIfBlank(PropertiesManager.getValue(PropertiesManager.KEY_MAIL_SERVER_PORT));
		String serverUser = StringProcessor.nullIfBlank(PropertiesManager.getValue(PropertiesManager.KEY_MAIL_SERVER_USER));
		String serverPW = StringProcessor.nullIfBlank(PropertiesManager.getValue(PropertiesManager.KEY_MAIL_SERVER_PW));
		String useSSL = PropertiesManager.getValue(PropertiesManager.KEY_MAIL_USE_SSL);
		String useTLS = PropertiesManager.getValue(PropertiesManager.KEY_MAIL_USE_TLS);

		if (StringUtils.isNotBlank(server)) {
			TransportStrategy transportStrategy = TransportStrategy.SMTP_PLAIN;
			if (Convert.toBoolean(useSSL)) {
				transportStrategy = TransportStrategy.SMTP_SSL;
			} else if (Convert.toBoolean(useTLS)) {
				transportStrategy = TransportStrategy.SMTP_TLS;
			}

			mailer = new Mailer(server, Convert.toInteger(serverPort), serverUser, serverPW, transportStrategy);
		} else {
			log.log(Level.WARNING, "No mail server is set up.  See application properties file to configure.");
		}
	}

	public static void cleanup()
	{
		//nothing needed for now
	}

	public static Email newEmail()
	{
		String fromName = PropertiesManager.getValue(PropertiesManager.KEY_MAIL_FROM_NAME, OpenStorefrontConstant.NOT_AVAILABLE);
		String fromAddress = PropertiesManager.getValue(PropertiesManager.KEY_MAIL_FROM_ADDRESS, OpenStorefrontConstant.DEFAULT_FROM_ADDRESS);
		String fromReply = PropertiesManager.getValue(PropertiesManager.KEY_MAIL_REPLY_NAME);
		String fromReplyAddress = PropertiesManager.getValue(PropertiesManager.KEY_MAIL_REPLY_ADDRESS);

		Email email = new Email();
		email.setFromAddress(fromName, fromAddress);
		if (StringUtils.isNotBlank(fromReplyAddress)) {
			email.setReplyToAddress(fromReply, fromReplyAddress);
		}
		return email;
	}

        /**
         * Sends an email
         * <p>
         * Performs validation on recipient email addresses
         * prior to sending. Catches and logs any exceptions
         * thrown during the sending of the email.
         * 
         * @param email An email object which is pre-configured
         * and ready to be sent
         */
	public static void send(Email email)
	{
                // Check For Null Email Object
		if (email != null) {
                        
                        // Check For Null Mailer Service
			if (mailer != null) {
                            
                                // Validate Recipients
                                List<Recipient> recipients = validateRecipients(email.getRecipients());
                                
                                // Check For Recipients
                                if (!recipients.isEmpty()) {
                                    
                                        // Adjust Recipients
                                        email.getRecipients().retainAll(recipients);

                                        // Attempt To Send Email
                                        try {

                                                // Send Email
                                                mailer.sendMail(email);
                                        }
                                        
                                        // Catch Mail Error
                                        catch (MailException e) {

                                                // Log Error
                                                log.log(Level.SEVERE, "An error occurred while sending email. The error message follows: {0}", e.getMessage());
                                        }
                                }
			}
                        else {
                                // Initialize Recipients String
				StringBuilder sb = new StringBuilder();
                                
                                // Check Recipients
                                email.getRecipients().forEach(recipient -> {
                                    
                                        // Store Recipient Type & Colon
                                        sb.append(recipient.getType()).append(": ");
                                        
                                        // Store Recipient Address & Separator (Comma)
                                        sb.append(recipient.getAddress()).append(", ");
                                });
                                
                                // Log Recipients
				log.log(Level.FINE, MessageFormat.format("(Mock Email Handler) Sending Message Subject: {0} To {1}", new Object[]{email.getSubject(), sb.toString()}));
			}
		}
                else {
                        
                        // Log Error
			log.log(Level.FINE, "Unable to send NULL email message. No message to send.");
		}
	}
        
        /**
         * Validate email addresses
         * <p>
         * Receives a list of recipients and validates each of their email addresses.
         * Returns a list of recipients whose email addresses passed validation
         * and logs those whose did not.
         * 
         * @param recipients A list of recipients who are intended to receive an email
         * @return A list of recipients which passed email address validation. An empty
         * list will be returned in the event that no recipient passed validation.
         */
        public static List<Recipient> validateRecipients(List<Recipient> recipients) {
            
                // Initialize Failed Recipient Flag
                boolean recipientsFailed = false;
            
                // Create New Recipient List
                List<Recipient> validRecipients = new ArrayList<>();
                
                // Initialize Failed Recipients String
                StringBuilder failedRecipients = new StringBuilder();
                
                // Loop Through Recipients
                for (Recipient recipient : recipients) {
                        
                        // Validate Email
                        if (EmailAddressValidator.isValid(recipient.getAddress())) {
                            
                                // Add Recipient
                                validRecipients.add(recipient);
                        }
                        else {
                                
                                // Indicate Recipient Failed
                                recipientsFailed = true;
                                
                                // Check For Existing Failed Recipient
                                if (failedRecipients.length() != 0) {

                                    // Append Comma
                                    failedRecipients.append(", ");
                                }

                                // Append Failed Recipient Name
                                failedRecipients.append(recipient.getName());

                                // Append Colon
                                failedRecipients.append(": ");

                                // Append Failed Recipient's Invalid Email Address
                                failedRecipients.append(recipient.getAddress());
                        }
                }
                
                // Check For Failed Recipients
                if (recipientsFailed) {
                    
                    // Log Failed Recipients
                    log.log(Level.WARNING, "Some recipient email addresses failed validation. The following are invalid: {0}", failedRecipients.toString());
                }
                
                // Return New Recipients
                return validRecipients;
        }

	@Override
	public void initialize()
	{
		MailManager.init();
		started.set(true);		
	}

	@Override
	public void shutdown()
	{
		MailManager.cleanup();
		started.set(false);
	}

	@Override
	public boolean isStarted()
	{
		return started.get();
	}	
	
}
