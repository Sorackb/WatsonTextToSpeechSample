package org.lucassouza.watsontexttospeechsample;

import com.ibm.watson.developer_cloud.http.ServiceCall;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class WatsonTextToSpeechSample {

  private final TextToSpeech service;
  private final LineListener listener;

  private enum VoiceOption {
    DE_DE_BIRGITVOICE("de-DE_BirgitVoice", "German", "Female"),
    DE_DE_DIETERVOICE("de-DE_DieterVoice", "German", "Male"),
    EN_GB_KATEVOICE("en-GB_KateVoice", "English (British dialect)", "Female"),
    EN_US_ALLISONVOICE("en-US_AllisonVoice", "English (US dialect)", "Female"),
    EN_US_LISAVOICE("en-US_LisaVoice", "English (US dialect)", "Female"),
    EN_US_MICHAELVOICE("en-US_MichaelVoice", "English (US dialect) (Default)", "Male"),
    ES_ES_ENRIQUEVOICE("es-ES_EnriqueVoice", "Spanish (Castilian dialect)", "Male"),
    ES_ES_LAURAVOICE("es-ES_LauraVoice", "Spanish (Castilian dialect)", "Female"),
    ES_LA_SOFIAVOICE("es-LA_SofiaVoice", "Spanish (Latin American dialect)", "Female"),
    ES_US_SOFIAVOICE("es-US_SofiaVoice", "Spanish (North American dialect)", "Female"),
    FR_FR_RENEEVOICE("fr-FR_ReneeVoice", "French", "Female"),
    IT_IT_FRANCESCAVOICE("it-IT_FrancescaVoice", "Italian", "Female"),
    JA_JP_EMIVOICE("ja-JP_EmiVoice", "Japanese", "Female"),
    PT_BR_ISABELAVOICE("pt-BR_IsabelaVoice", "Brazilian Portuguese", "Female");

    private final String voice;
    private final String language;
    private final String gender;

    private VoiceOption(String voice, String language, String gender) {
      this.voice = voice;
      this.language = language;
      this.gender = gender;
    }

    public String getVoice() {
      return this.voice;
    }
  }

  public WatsonTextToSpeechSample() {
    this.service = new TextToSpeech("CREDENTIAL USERNAME", "CREDENTIAL PASSWORD");

    this.listener = (LineEvent evento) -> {
      if (evento.getType() == LineEvent.Type.STOP) {
        evento.getLine().close();
      }
    };
  }

  public void synthesize(String text, VoiceOption voiceOption) {
    Map<String, String> options = new HashMap<>();
    ServiceCall<InputStream> call;
    InputStream result;
    AudioInputStream stream;
    Clip clip;
    Voice voice;
    AudioFormat audioFormat;

    // Realiza a chamada do serviço
    voice = new Voice(voiceOption.getVoice(), null, null);
    audioFormat = new AudioFormat("audio/wav");
    options.put("accept", "audio/wav;rate=8000");
    this.service.setDefaultHeaders(options);
    call = this.service.synthesize(text, voice, audioFormat);
    result = call.execute(); // Aguarda o retorno

    try {
      stream = AudioSystem.getAudioInputStream(WaveUtils.reWriteWaveHeader(result));
      clip = AudioSystem.getClip();
      clip.open(stream);
      clip.addLineListener(listener);
      clip.start(); // Inicia a execução do som
      clip.drain(); // Aguarda a execução
    } catch (IOException | LineUnavailableException | UnsupportedAudioFileException ex) {
      Logger.getLogger(WatsonTextToSpeechSample.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public static void main(String[] args) {
    WatsonTextToSpeechSample teste = new WatsonTextToSpeechSample();

    teste.synthesize("Meu texto", VoiceOption.PT_BR_ISABELAVOICE);
    teste.synthesize("Meu texto".replace("", " "), VoiceOption.PT_BR_ISABELAVOICE);
  }
}
