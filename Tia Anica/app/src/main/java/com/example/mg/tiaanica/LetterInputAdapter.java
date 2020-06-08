package com.example.mg.tiaanica;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import static android.view.View.generateViewId;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

public class LetterInputAdapter extends RecyclerView.Adapter<LetterInputAdapter.LetterInputViewHolder> {

    private List<String> neededLetters;
    private HashMap<String, Double> variableValues;
    DoneWithInput doneWithInputDelegate;

    LetterInputAdapter(List<String> neededLetters, HashMap<String, Double> variableValues, DoneWithInput doneWithInputDelegate){
        this.neededLetters = neededLetters;
        this.variableValues = variableValues;
        this.doneWithInputDelegate = doneWithInputDelegate;
    }

    @NonNull
    @Override
    public LetterInputAdapter.LetterInputViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.letter_input, viewGroup, false);
        return new LetterInputViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LetterInputAdapter.LetterInputViewHolder viewHolder, int position) {

        String letter = neededLetters.get(position);
        viewHolder.letter.setText(letter.toUpperCase());

        if(position == getItemCount() - 1){
            viewHolder.letterValue.setImeOptions(IME_ACTION_DONE);
            viewHolder.letterValue.setOnEditorActionListener((v, actionId, event) -> {
                if(actionId == IME_ACTION_DONE){
                    doneWithInputDelegate.doneWithInput();
                    return true;
                }
                return false;
            });
        }

        // If we already have a value for this letter then fill in the edittext
        if(variableValues.containsKey(letter)){
            double letterValue = variableValues.get(letter);
            String letterValueStr = (letterValue % 1 == 0) ? Integer.toString((int) letterValue) : Double.toString(letterValue);
            viewHolder.letterValue.setText(letterValueStr);
        }

        viewHolder.letterValue.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String valueString = viewHolder.letterValue.getText().toString();
                // TODO: if string is "" display warning
                if(valueString.equals("")) return;
                double value = Double.parseDouble(valueString);
                String currentLetter = viewHolder.letter.getText().toString();
                variableValues.put(currentLetter, value);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) { }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    @Override
    public int getItemCount() {
        return neededLetters.size();
    }

    public static class LetterInputViewHolder extends  RecyclerView.ViewHolder{

        TextView letter;
        EditText letterValue;

        public LetterInputViewHolder(@NonNull View itemView) {
            super(itemView);

            letter = itemView.findViewById(R.id.letter);
            letterValue = itemView.findViewById(R.id.letter_value);
            letterValue.setId(generateViewId());
        }
    }
}
