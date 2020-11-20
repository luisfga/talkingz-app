package br.com.luisfga.talkingz.app.ui.directmessage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import br.com.luisfga.talkingz.app.R;
import br.com.luisfga.talkingz.app.database.entity.message.DirectMessage;
import br.com.luisfga.talkingz.app.utils.BitmapUtility;
import br.com.luisfga.talkingz.commons.constants.MessageStatus;
import br.com.luisfga.talkingz.commons.constants.Mimetype;

public class DirectMessageListAdapter extends BaseAdapter {

    private List<DirectMessage> mMessages;
    private Context context;
    private UUID userId;
    private final Locale localeBR = new Locale("pt", "BR");
    private final DateFormat hourFormat = new SimpleDateFormat("HH:mm", localeBR);
    private final DateFormat dateFormat = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", localeBR);

    DirectMessageListAdapter(Context context, UUID userId) {
        this.context = context;
        this.userId = userId;
    }

    @Override
    public int getCount() {
        if (mMessages != null)
            return mMessages.size();
        else return 0;
    }

    @Override
    public Object getItem(int position) {
        return mMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public boolean isSameDay(long millisItemOne, long millisItemTwo) {
        Calendar thenCal = new GregorianCalendar();
        thenCal.setTimeInMillis(millisItemOne);

        Calendar nowCal = new GregorianCalendar();
        nowCal.setTimeInMillis(millisItemTwo);

        //se for o mesmo dia, esconder header
        return thenCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) //same year
                && thenCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH) //same month
                && thenCal.get(Calendar.DAY_OF_MONTH) == nowCal.get(Calendar.DAY_OF_MONTH); //same day
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.activity_direct_message_list_item, null, true);
        }

        TextView directMessageHeaderContent = convertView.findViewById(R.id.direct_message_header_content);

        LinearLayout directMessageContainer = convertView.findViewById(R.id.direct_message_container);
        TextView directMessageContent = convertView.findViewById(R.id.direct_message_content);
        TextView directMessageDate = convertView.findViewById(R.id.direct_message_date);
        ImageView directMessageMediaThumbnail = convertView.findViewById(R.id.direct_message_media);
        ImageView directMessageMediaPlayIcon = convertView.findViewById(R.id.direct_message_media_play_icon);

        int directMessageContainerMargin = 10;

        if (mMessages != null) {

            DirectMessage currentItem = mMessages.get(position);

            //coloca o Header com a data quando o dia mudar
            if (position > 0) {
                long millisItemAnterior = mMessages.get(position-1).getSentTime().getTime();
                long millisItemCorrente = mMessages.get(position).getSentTime().getTime();
                if (isSameDay(millisItemAnterior, millisItemCorrente)) {
                    directMessageHeaderContent.setVisibility(View.GONE);
                } else {
                    String data = dateFormat.format(new Date(currentItem.getSentTime().getTime())); //TODO corrigir pra TimeStamp
                    directMessageHeaderContent.setText(data);
                    directMessageHeaderContent.setVisibility(View.VISIBLE);
                }
            } else {
                String data = dateFormat.format(new Date(currentItem.getSentTime().getTime()));
                directMessageHeaderContent.setText(data);
                directMessageHeaderContent.setVisibility(View.VISIBLE);
            }

            //joga pra mensagem pra um lado ou outro, dependendo se o rementente for o usuário ou interlocutores

            if(isFromUser(currentItem)) { //por padrão não precisa mudar o Drawable do background quando a mensagem é do usuário local.
                directMessageContent.setText(currentItem.getContent());

                //altera imagem de fundo do container da mensagem, para que a seta aponte para o lado do interlocutor
                directMessageContainer.setBackgroundResource(R.drawable.bubble_l);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_START);
                params.setMargins(directMessageContainerMargin,directMessageContainerMargin,directMessageContainerMargin,directMessageContainerMargin);
                directMessageContainer.setLayoutParams(params);

                Drawable receivedCheck = context.getDrawable(R.drawable.ic_check_12dp);
                if (currentItem.getStatus() == MessageStatus.MSG_STATUS_DELIVERED) {
                    receivedCheck.setTint(context.getResources().getColor(R.color.vividGreen));
                } else if (currentItem.getStatus() == MessageStatus.MSG_STATUS_ON_TRAFFIC) {
                    receivedCheck.setTint(context.getResources().getColor(R.color.babyblue));
                } else {
                    receivedCheck.setTint(context.getResources().getColor(R.color.darkGray));
                }
                directMessageDate.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null, receivedCheck,null);

            } else {//joga a mensagem pra direita, no caso de mensagem recebida
                directMessageContent.setText(currentItem.getContent());

                //altera imagem de fundo do container da mensagem, para que a seta aponte para o lado do interlocutor
                directMessageContainer.setBackgroundResource(R.drawable.bubble_r);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
                params.setMargins(directMessageContainerMargin,directMessageContainerMargin,directMessageContainerMargin,directMessageContainerMargin);
                directMessageContainer.setLayoutParams(params);

                directMessageDate.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null, null,null);
            }

            //horário da mensagem
            String horario = hourFormat.format(new Date(currentItem.getSentTime().getTime()));
            directMessageDate.setText(horario);

            //set media if exists
            //TODO reformar esses ifs
            if(currentItem.getMimeType() != Mimetype.TXT) {
                directMessageMediaThumbnail.setVisibility(View.VISIBLE);

                Bitmap bitmap = BitmapUtility.getBitmapFromBytes(currentItem.getMediaThumbnail());
                directMessageMediaThumbnail.setImageBitmap(bitmap);

                if(currentItem.getMimeType() == Mimetype.IMAGE_GENERIC){
                    directMessageMediaPlayIcon.setVisibility(View.GONE);
                    directMessageMediaThumbnail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //TODO arrumar ACTION_VIEW
                            //TODO ver como colocar uma barra de progresso
//                            if (!FileUtility.fileExists(context, currentItem.getMediaFilename())) {
//                                if(!isFromUser(currentItem)) {
//                                    CommandGetFile commandGetFile = new CommandGetFile();
//                                    commandGetFile.setFileName(currentItem.getMediaFilename());
//                                    ((DirectMessageActivity)context).sendCommandGetFile(currentItem.getMediaFilename());
//                                } else {
//                                    //se esse arquivo era do próprio usuário e não existe é porque ele mesmo deletou, portanto não será baixado
//                                    Toast.makeText(context, "Arquivo não encontrado", Toast.LENGTH_SHORT).show();
//                                }
//
//                            } else {
//                                try {
//                                    FileUtility.openMediaFile(context, currentItem.getMediaFilename());
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
                        }
                    });

                } else if (currentItem.getMimeType() == Mimetype.VIDEO_MPEG) {
                    directMessageMediaPlayIcon.setVisibility(View.VISIBLE);
                    directMessageMediaThumbnail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //TODO arrumar ACTION_VIEW
                            //TODO ver como colocar uma barra de progresso
//                            if (!FileUtility.fileExists(context, currentItem.getMediaFilename())) {
//                                if(!isFromUser(currentItem)) {
//                                    CommandGetFile commandGetFile = new CommandGetFile();
//                                    commandGetFile.setFileName(currentItem.getMediaFilename());
//                                    ((DirectMessageActivity)context).sendCommandGetFile(currentItem.getMediaFilename());
//                                } else {
//                                    //se esse arquivo era do próprio usuário e não existe é porque ele mesmo deletou, portanto não será baixado
//                                    Toast.makeText(context, "Arquivo não encontrado", Toast.LENGTH_SHORT).show();
//                                }
//
//                            } else {
//                                try {
//                                    FileUtility.openMediaFile(context, currentItem.getMediaFilename());
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
                        }
                    });

                }

            } else {
                directMessageMediaThumbnail.setVisibility(View.GONE);
                directMessageMediaPlayIcon.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    private boolean isFromUser(DirectMessage directMessage) {
        //se o destino é o contato, então a mensagem partiu do usuário do app cliente.
        return directMessage.getSenderId().equals(userId);
    }

    public void setItems(List<DirectMessage> items) {
        mMessages = items;
        notifyDataSetChanged();
    }

}
