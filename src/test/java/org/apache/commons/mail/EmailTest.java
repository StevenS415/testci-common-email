package org.apache.commons.mail;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class EmailTest {

	private Email email;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void Setup() {
		email = new EmailDummy();
	}

/////// addBcc(String ... email) ///////
	@Test
	/*
	 * Test to ensure the addresses are all correctly added to the bcc list The
	 * test case succeeds if all of the test bcc addresses input into the method
	 * match the stored bccAddresses values fetched from getBccAddresses.
	 */
	public void addBccTest() {
		String[] addresses = { "abc@def", "ghi@jkl" };
		try {
			email.addBcc(addresses);
			List<InternetAddress> bcc = email.getBccAddresses();

			for (int i = 0; i < addresses.length; i++)
				assertEquals(addresses[i], bcc.get(i).toString());
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	@Test(expected = EmailException.class)
	/*
	 * Test to ensure an EmailException is thrown when an empty array is passed
	 * in
	 */
	public void addBccExceptionEmptyTest() throws EmailException {
		String[] addresses = {};
		email.addBcc(addresses);
	}

	@Test(expected = EmailException.class)
	/*
	 * Test to ensure an EmailException is thrown when a null array is passed in
	 */
	public void addBccExceptionNullTest() throws EmailException {
		String[] addresses = null;
		email.addBcc(addresses);
	}

/////// addCc(String email) ///////
	@Test
	/*
	 * Test to ensure the address is correctly added to the cc list
	 */
	public void addCcTest() {
		try {
			email.addCc("abc@def");
			List<InternetAddress> cc = email.getCcAddresses();
			assertEquals("abc@def", cc.get(0).toString());
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	@Test(expected = EmailException.class)
	/**
	 * Test to ensure an EmailException is thrown when an invalid email is
	 * passed in.
	 */
	public void addCcInvalidEmailTest() throws EmailException {
		email.addCc("abc");
	}

/////// addHeader(String email, String value) ///////
	@Test
	/*
	 * Test to ensure the (name, value) pair is correctly added to the header
	 * hash map.
	 */
	public void addHeaderTest() {
		try {
			email.addHeader("ABC", "def");
			assertEquals("def", email.headers.get("ABC"));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	@Test
	/**
	 * Test to ensure an IllegalArgumentException with the specified message is
	 * thrown when a null value is passed in for the name.
	 */
	public void addHeaderWithNullNameTest() throws IllegalArgumentException {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("name can not be null or empty");

		email.addHeader(null, "def");
	}

	@Test
	/**
	 * Test to ensure an IllegalArgumentException with the specified message is
	 * thrown when a null value is passed in for the value.
	 */
	public void addHeaderWithNullValueTest() throws IllegalArgumentException {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("value can not be null or empty");

		email.addHeader("ABC", null);
	}

/////// addReplyTo(String email, String name) ///////
	@Test
	/**
	 * Test to ensure the reply address/name pair is added to the reply list.
	 */
	public void addReplyToTest() {
		try {
			email.addReplyTo("abc@def", "abc");
			List<InternetAddress> rep = email.getReplyToAddresses();

			// The pair is stored as "name[address]", so the address must be
			// retrieved to
			// check for equality
			assertEquals("abc@def", rep.get(0).getAddress());
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	@Test(expected = EmailException.class)
	/**
	 * Test to ensure an EmailException is thrown if the address is invalid.
	 */
	public void addReplyToInvalidAddressTest() throws EmailException {
		email.addReplyTo("abc", "abc");
	}

	@Test
	/**
	 * Test to ensure that an EmailException with the specified message is
	 * thrown if no host name is provided for the message as getMailSession is
	 * used, which requires a host be set.
	 */
	public void buildMimeMessageNoHostTest() throws EmailException {
		thrown.expect(EmailException.class);
		thrown.expectMessage("Cannot find valid hostname for mail session");

		email.buildMimeMessage();
	}

	@Test(expected = IllegalStateException.class)
	/**
	 * Test to ensure that an IllegalStateException is thrown if the Mime
	 * Message is created before buildMimeMessage is called.
	 */
	public void buildMimeMessageAlreadyBuiltTest()
			throws EmailException, IllegalStateException {
		email.hostName = "localhost";
		email.message = email.createMimeMessage(email.getMailSession());

		email.buildMimeMessage();
	}

	@Test
	/**
	 * Test to ensure that an EmailException with the specified message is
	 * thrown if no from address is provided.
	 */
	public void buildMimeMessageNoFromTest() throws EmailException {
		thrown.expect(EmailException.class);
		thrown.expectMessage("From address required");

		email.hostName = "localhost";

		email.buildMimeMessage();
	}

	@Test
	/**
	 * Test to ensure that an EmailException with the specified message is
	 * thrown if no recipient addresses, that is no bcc, cc, or to addresses,
	 * are provided.
	 */
	public void buildMimeMessageNoReceiverTest() throws EmailException {
		thrown.expect(EmailException.class);
		thrown.expectMessage("At least one receiver address required");

		email.hostName = "localhost";
		email.setFrom("abc@def");

		email.buildMimeMessage();
	}

	@Test
	/**
	 * Test to ensure that an added bcc address is correctly added to the
	 * recipients list.
	 */
	public void buildMimeMessageBccTest()
			throws EmailException, MessagingException {
		email.hostName = "localhost";
		email.setFrom("abc@def");
		email.addBcc("ghi@jkl");

		email.buildMimeMessage();

		Address[] recipients = email.message
				.getRecipients(Message.RecipientType.BCC);
		assertEquals("ghi@jkl", recipients[0].toString());
	}

	@Test
	/**
	 * Test to ensure that an added cc address is correctly added to the
	 * recipients list.
	 */
	public void buildMimeMessageCcTest()
			throws EmailException, MessagingException {
		email.hostName = "localhost";
		email.setFrom("abc@def");
		email.addCc("ghi@jkl");

		email.buildMimeMessage();

		Address[] recipients = email.message
				.getRecipients(Message.RecipientType.CC);
		assertEquals("ghi@jkl", recipients[0].toString());
	}

	@Test
	/**
	 * Test to ensure that an added "to" address is correctly added to the
	 * recipients list.
	 */
	public void buildMimeMessageToTest()
			throws EmailException, MessagingException {
		email.hostName = "localhost";
		email.setFrom("abc@def");
		email.addTo("ghi@jkl");

		email.buildMimeMessage();

		Address[] recipients = email.message
				.getRecipients(Message.RecipientType.TO);
		assertEquals("ghi@jkl", recipients[0].toString());
	}

	@Test
	/**
	 * Test to ensure that the message can still be successfully built if the
	 * from address is included as a session property.
	 */
	public void buildMimeMessagePropertyFromTest()
			throws EmailException, MessagingException {
		email.hostName = "localhost";

		Properties prop = new Properties();
		prop.setProperty(email.MAIL_SMTP_FROM, "abc@def");
		Session scn = Session.getInstance(prop);
		email.setMailSession(scn);

		email.addTo("ghi@jkl");

		email.buildMimeMessage();

		assertNotEquals(null, email.message);
	}

	@Test
	/**
	 * Test to ensure that added headers are correctly stored in the message.
	 */
	public void buildMimeMessageHeaderTest()
			throws EmailException, MessagingException {
		email.hostName = "localhost";
		email.setFrom("abc@def");
		email.addTo("ghi@jkl");

		email.addHeader("Content-Type", "text/plain");

		email.buildMimeMessage();

		assertEquals("text/plain", email.message.getHeader("Content-Type")[0]);
	}

	@Test
	/**
	 * Test to ensure that added plain text message content is correctly stored
	 * in the message, as well as the content type.
	 */
	public void buildMimeMessagePlainTextContentTest()
			throws EmailException, IOException, MessagingException {
		email.hostName = "localhost";
		email.setFrom("abc@def");
		email.addTo("ghi@jkl");

		String text = "Test Content 123";
		email.setContent(text, "text/plain");

		// Because MimeMessage.getContentType() retrieves the "Content-Type"
		// header value,such header must be present, but none of the methods
		// used in buildMimeMessage() do so.
		email.addHeader("Content-Type", "text/plain");

		email.buildMimeMessage();

		assertEquals(text, email.message.getContent().toString());
		assertEquals("text/plain", email.message.getContentType());
	}

	@Test
	/**
	 * Test to ensure that added html message content is correctly stored in the
	 * message, as well as the content type.
	 */
	public void buildMimeMessageHtmlTextContentTest()
			throws EmailException, IOException, MessagingException {
		email.hostName = "localhost";
		email.setFrom("abc@def");
		email.addTo("ghi@jkl");

		String text = "<p>Test Content 123</p>";
		String type = "text/html";
		email.setContent(text, type);

		email.addHeader("Content-Type", "text/html");

		email.buildMimeMessage();

		assertEquals(text, email.message.getContent().toString());
		assertEquals("text/html", email.message.getContentType());
	}

	@Test
	/**
	 * Test to ensure that added content that is not a string is correctly
	 * stored in the message.
	 */
	public void buildMimeMessageContentNotStringTest()
			throws EmailException, IOException, MessagingException {
		email.hostName = "localhost";
		email.setFrom("abc@def");
		email.addTo("ghi@jkl");

		int text = 123;
		email.setContent(text, "text/plain");

		email.buildMimeMessage();

		// Convert integer to string as Object cannot convert to integer, only
		// to String
		assertEquals(Integer.toString(text),
				email.message.getContent().toString());
	}

	@Test
	/**
	 * Test to ensure that an added subject is correctly stored as the message
	 * subject.
	 */
	public void buildMimeMessageSubjectTest()
			throws EmailException, MessagingException {
		email.hostName = "localhost";
		email.setFrom("abc@def");
		email.addTo("ghi@jkl");

		email.setSubject("Test Message");

		email.buildMimeMessage();

		assertEquals("Test Message", email.message.getSubject());
	}

	@Test
	/**
	 * Test to ensure that an added subject is correctly stored as the message
	 * subject, and that an added charset to be used for the subject is
	 * correctly stored.
	 */
	public void buildMimeMessageSubjectWithCharsetTest()
			throws EmailException, MessagingException {
		email.hostName = "localhost";
		email.setFrom("abc@def");
		email.addTo("ghi@jkl");

		email.setSubject("Test Message");
		email.setCharset("US-ASCII");

		email.buildMimeMessage();

		assertEquals("Test Message", email.message.getSubject());
		assertEquals("US-ASCII", email.charset);
	}

	@Test
	/**
	 * Test to ensure that and added "reply to" address is correctly added to
	 * the message's list of "reply to" addresses.
	 */
	public void buildMimeMessageReplyToTest()
			throws EmailException, MessagingException {
		email.hostName = "localhost";
		email.setFrom("abc@def");
		email.addTo("ghi@jkl");

		email.addReplyTo("mno@pqr");

		email.buildMimeMessage();

		assertEquals("mno@pqr", email.message.getReplyTo()[0].toString());

	}

	@Test
	/**
	 * Test to ensure that a MimeMultipart will be correctly added as the
	 * message content.
	 * 
	 */
	public void buildMimeMessageMultiparteTest()
			throws EmailException, MessagingException, IOException {
		email.hostName = "localhost";
		email.setFrom("abc@def");
		email.addTo("ghi@jkl");

		MimeMultipart part = new MimeMultipart();
		email.emailBody = part;

		email.buildMimeMessage();

		assertEquals(part, email.message.getContent());
	}

	@Test
	/**
	 * Test to ensure that popBeforeSmtp will be true, and thus enter the
	 * appropriate branch. Because there is no true email being sent, nor a real
	 * host to connect to via pop3, an error must be thrown, thus also testing
	 * to ensure a MessagingException can be thrown.
	 */
	public void buildMimeMessagePopBeforeSmtpExceptionTest()
			throws EmailException {
		thrown.expect(EmailException.class);
		thrown.expectMessage("failed to connect");
		email.hostName = "localhost";
		email.setFrom("abc@def");
		email.addTo("ghi@jkl");

		email.setPopBeforeSmtp(true, null, null, null);

		email.buildMimeMessage();
	}

/////// getHostName() ///////
	@Test
	/**
	 * Test to ensure the host name can be retrieved if it has been initialized
	 */
	public void getHostNameTest() {
		email.hostName = "localhost";
		String host = email.getHostName();

		assertEquals("localhost", host);
	}

	@Test
	/**
	 * Test to ensure the host name will be retrieved as null if it has not been
	 * initialized.
	 */
	public void getHostNameNullHostTest() {
		String host = email.getHostName();

		assertEquals(null, host);
	}

	@Test
	/**
	 * Test to ensure the host name can be retrieved from an input session
	 */
	public void getHostNameWithSessionTest() {
		Properties prop = new Properties();
		prop.setProperty(email.MAIL_HOST, "localhost");
		Session scn = Session.getInstance(prop);
		email.setMailSession(scn);

		String host = email.getHostName();
		assertEquals("localhost", host);
	}

/////// getMailSession() ///////
	@Test
	/**
	 * Test to ensure the mail session is initialized correctly with the set
	 * host name.
	 */
	public void getMailSessionTest() {
		try {
			email.hostName = "localhost";
			Session mail_scn = email.getMailSession();
			assertEquals("localhost", mail_scn.getProperty(email.MAIL_HOST));
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getMailSessionWithStartTLSEnableTest() {
		try {
			email.hostName = "localhost";
			email.setStartTLSEnabled(true);
			Session mail_scn = email.getMailSession();

			assertEquals("true", mail_scn.getProperty(
					EmailConstants.MAIL_TRANSPORT_STARTTLS_ENABLE));
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getMailSessionWithStartTLSRequiredTest() {
		try {
			email.hostName = "localhost";
			email.setStartTLSRequired(true);
			Session mail_scn = email.getMailSession();

			assertEquals("true", mail_scn.getProperty(
					EmailConstants.MAIL_TRANSPORT_STARTTLS_REQUIRED));
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getMailSessionWithSendPartialTest() {
		try {
			email.hostName = "localhost";
			email.setSendPartial(true);
			Session mail_scn = email.getMailSession();

			assertEquals("true", mail_scn
					.getProperty(EmailConstants.MAIL_SMTP_SEND_PARTIAL));
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getMailSessionWithAutheticatorTest() {
		try {
			email.hostName = "localhost";
			// Set up authenticator with a username and password
			email.setAuthentication("abc", "test123");
			Session mail_scn = email.getMailSession();

			assertEquals("true",
					mail_scn.getProperty(EmailConstants.MAIL_SMTP_AUTH));
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getMailSessionWithSSLTest() {
		try {
			email.hostName = "localhost";
			// Set up authenticator with a username and password
			email.setSSLOnConnect(true);
			Session mail_scn = email.getMailSession();

			assertEquals("false", mail_scn
					.getProperty(email.MAIL_SMTP_SOCKET_FACTORY_FALLBACK));
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getMailSessionWithSSLandSSLCheckTest() {
		try {
			email.hostName = "localhost";

			email.setSSLOnConnect(true);
			email.setSSLCheckServerIdentity(true);
			Session mail_scn = email.getMailSession();

			assertEquals("true", mail_scn.getProperty(
					EmailConstants.MAIL_SMTP_SSL_CHECKSERVERIDENTITY));
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getMailSessionWithBounceAddressTest() {
		try {
			email.hostName = "localhost";

			email.setBounceAddress("abc@def");
			Session mail_scn = email.getMailSession();

			assertEquals("abc@def", mail_scn.getProperty(email.MAIL_SMTP_FROM));
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getMailSessionWithoutSocketTimeoutTest() {
		try {
			email.hostName = "localhost";

			email.setSocketTimeout(0);
			Session mail_scn = email.getMailSession();

			// This branch should not have this property, so getProperty should
			// return null
			assertEquals(null, mail_scn.getProperty(email.MAIL_SMTP_TIMEOUT));
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getMailSessionWithoutSocketConnectionTimeoutTest() {
		try {
			email.hostName = "localhost";

			email.setSocketConnectionTimeout(0);
			Session mail_scn = email.getMailSession();

			// This branch should not have this property, so getProperty should
			// return null
			assertEquals(null,
					mail_scn.getProperty(email.MAIL_SMTP_CONNECTIONTIMEOUT));
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	@Test(expected = EmailException.class)
	public void getMailSessionNoHostTest() throws EmailException {
		email.getMailSession();
	}

	@Test
	public void getMailSessionExistingSessionTest() {
		Properties prop = new Properties();
		Session scn = Session.getInstance(prop);
		email.setMailSession(scn);

		Session mail_scn;
		try {
			mail_scn = email.getMailSession();
			assertEquals(scn, mail_scn);
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

/////// getSentDate() ///////
	@Test
	public void getSentDateCurrentDateTest() {
		Date currDate = new Date();

		Date date = email.getSentDate();
		assertEquals(currDate, date);

	}

	@Test
	public void getSentDateMiscDateTest() {
		Date miscDate = new Date(987292800979L);
		email.setSentDate(miscDate);

		Date date = email.getSentDate();
		assertEquals(miscDate, date);

	}

/////// getSocketConnectionTimeout() ///////
	@Test
	public void getSocketConnectionTimeoutTest() {
		int socketConnectionTimeout = email.socketConnectionTimeout;
		int timeout = email.getSocketConnectionTimeout();

		assertEquals(socketConnectionTimeout, timeout);
	}

/////// setFrom(String email) ///////
	@Test
	public void setFromTest() {
		try {
			email.setFrom("abc@def");
			InternetAddress from = email.getFromAddress();

			assertEquals("abc@def", from.toString());
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	@Test(expected = EmailException.class)
	/*
	 * 
	 */
	public void setFromInvalidTest() throws EmailException {
		email.setFrom("abc");
	}

	@After
	public void TearDown() {

	}
}
