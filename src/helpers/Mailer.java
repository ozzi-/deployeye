package helpers;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mailer {

    private static HashMap<String, String> subjectsLookup = new HashMap<String, String>();
    private static HashMap<String, String> contentsLookup = new HashMap<String, String>();

	public static void sendMailAsync(String to, String subject, String content) {
		String subjectFinal = org.apache.commons.text.StringEscapeUtils.unescapeJava(subject);
		String contentFinal = org.apache.commons.text.StringEscapeUtils.unescapeJava(content);
		Runnable mailSender = new Runnable() {
			public void run() {
				Mailer.send(to, subjectFinal, contentFinal);
			}
		};
		new Thread(mailSender).start();
	}
	
	private static boolean sendSimpleMailSMTPInternal(String smtpHost, int smtpPort, String smtpUser, String smtpPassword,
			boolean smtpAuth, boolean smtpSecure, boolean trustAllCertificates, String from, String to, String subject, String body, String header) {
		try {
			Properties props = System.getProperties();
			props.put("mail.smtp.host", smtpHost);
			props.put("mail.smtp.port", smtpPort);
			props.put("mail.smtp.auth", smtpAuth);
			if(smtpSecure) {
				props.put("mail.smtp.starttls.enable", "true");
			}
			if(trustAllCertificates) {
			    props.put("mail.smtp.ssl.trust", "*");
			}

			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(smtpUser, smtpPassword);
				}
			});
			MimeMessage msg = new MimeMessage(session);
			msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");
			if(header != null && !header.equals("") && header.contains(":") && header.length()>3) {
				String headerName	= header.substring(0, header.indexOf(":"));
				String headerValue	= header.substring(header.indexOf(":"));
				msg.addHeader(headerName, headerValue);				
			}
			msg.setFrom(new InternetAddress(from, from));
			msg.setReplyTo(InternetAddress.parse(from, false));
			msg.setSubject(subject, "UTF-8");
			msg.setText(body, "UTF-8");
			msg.setSentDate(new Date());
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
			Transport.send(msg);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.logException(e,Mailer.class);
		}
		return false;
	}

	
	public static void send(String to, String subject, String content) {
		Log.logInfo("Sending mail to '"+to+"' with subject '"+subject+"' and content '"+content+"'.", Mailer.class);
		sendSimpleMailSMTPInternal(	Config.getMailAccount().getHostSmtp(), Config.getMailAccount().getPortSmtp(), Config.getMailAccount().getLogin(), Config.getMailAccount().getPw(), true, Config.getMailAccount().isSecureSmtp(), Config.getMailAccount().isTrustAllCerts(), Config.getMailAccount().getAddress(), to, subject, content, null);		
	}

	public static void send(String to, String subject, String content, String header) {
		sendSimpleMailSMTPInternal(Config.getMailAccount().getHostSmtp(), Config.getMailAccount().getPortSmtp(), Config.getMailAccount().getLogin(), Config.getMailAccount().getPw(), true, Config.getMailAccount().isSecureSmtp(), Config.getMailAccount().isTrustAllCerts(), Config.getMailAccount().getAddress(), to, subject, content, header);
	}

	
	public static void addToSubjectLookup(String key, String value) {
		subjectsLookup.put(key, value);
	}
	
	public static void addToContentsLookup(String key, String value) {
		contentsLookup.put(key, value);
	}
	
	public static String getSubject(String key) {
		String subj = subjectsLookup.get(key);
		if(subj==null) {
			Log.logException(new Exception("Could not find subject in lookup table - "+key+" seems to be missing in mailTemplates.json."), Mailer.class);
		}
		return subj;
	}

	public static String getContent(String key, ArrayList<String> values) {
		String content = contentsLookup.get(key);
		if(content==null) {
			Log.logException(new Exception("Could not find content in lookup table - "+key+" seems to be missing in mailTemplates.json."), Mailer.class);
		}
	    StringBuffer res=new StringBuffer();
		try {
			Matcher m = Pattern.compile("%([0-9]*)%").matcher(content);
		    while (m.find()) {
		    	int pos = Integer.valueOf(m.group(1))-1;
				try {
					String val = values.get(pos);
			    	m.appendReplacement(res, val);
				}catch (Exception e) {
					Log.logException(new Exception("Could not replace placeholder with value for index '"+(++pos)+"' for the key '"+key+"'"), Mailer.class);
				}
		    }
		    m.appendTail(res);
		}catch (Exception e) {
			Log.logException(new Exception(e.getClass().getCanonicalName()+" exception encountered trying to get content - "+e.getCause()), Mailer.class);
		}
		return res.toString();
	}
}
