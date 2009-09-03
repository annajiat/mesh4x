package org.mesh4j.ektoo;

import static org.mesh4j.ektoo.ui.settings.prop.AppPropertiesProvider.getProperty;

import java.awt.EventQueue;
import java.util.Enumeration;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mesh4j.ektoo.controller.EktooController;
import org.mesh4j.ektoo.ui.EktooFrame;
import org.mesh4j.ektoo.ui.component.messagedialog.MessageDialog;
import org.mesh4j.ektoo.ui.settings.prop.AppProperties;
import org.mesh4j.translator.EktooMessageTranslator;
import org.mesh4j.translator.MessageProvider;

/**
 * @author Bhuiyan Mohammad Iklash
 */
public class Ektoo {
	
	private final static Log LOGGER = LogFactory.getLog(Ektoo.class);
	private static EktooFrame parentUI= null; 
	
	public static void main(String[] args) {
		
		initInternationalization();
		initLookAndFeel();
		setUIFont(new FontUIResource(EktooMessageTranslator
				.translate("EKTOO_DEFAULT_UNICODE_FONT_NAME"), Integer
				.parseInt(EktooMessageTranslator
						.translate("EKTOO_DEFAULT_UNICODE_FONT_STYLE")),
				Integer.parseInt(EktooMessageTranslator
						.translate("EKTOO_DEFAULT_UNICODE_FONT_SIZE"))));
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new Ektoo().initUI();
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		});
	}

	private Ektoo(){ 
	}
	
	private void initUI(){
		EktooController controller = new EktooController();
		parentUI = new EktooFrame(controller);
		parentUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		parentUI.setVisible(true);
	}

	
	private static void initInternationalization(){

		String languageName = getProperty(AppProperties.LANGUAGE);
		if(languageName.equals(AppProperties.LANGUAGE_ENGLISH)){
			MessageProvider.init(MessageProvider.LANGUAGE_ENGLISH, 
					MessageProvider.COUNTRY_US);
		} else if (languageName.equals(AppProperties.LANGUAGE_SYSTEM_DEFAULT)){
			MessageProvider.init(Locale.getDefault());
		} else {
			//TODO improve , or we can load the os default locale
			//system not supported this type of locale
			MessageDialog.showErrorMessage(null, "Locale specific resource file" +
					" is not available \n application will exit");
			System.exit(0);
		}
	}
	
	//TODO (raju)improve
	public static EktooFrame getParentContainer(){
		return parentUI;
	}
	
	private static void initLookAndFeel(){ 
		try{ 
			String lookAndFeel= getProperty(AppProperties.LOOK_AND_FEEL_CLASS_NAME);
		  if (lookAndFeel != null && lookAndFeel.trim().length() != 0)
		    UIManager.setLookAndFeel(lookAndFeel);
		  else
		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e){ 
			LOGGER.error(e.getMessage(), e);
		} 
	}

	public static void setUIFont(javax.swing.plaf.FontUIResource f){ 
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource)
				UIManager.put(key, f);
		}
	}

}
