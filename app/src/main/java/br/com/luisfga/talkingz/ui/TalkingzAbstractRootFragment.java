package br.com.luisfga.talkingz.ui;

import androidx.fragment.app.Fragment;

import br.com.luisfga.talkingz.TalkingzApp;

public abstract class TalkingzAbstractRootFragment extends Fragment {

    protected TalkingzApp getTalkingzApp(){
        return (TalkingzApp) getActivity().getApplication();
    }
}
