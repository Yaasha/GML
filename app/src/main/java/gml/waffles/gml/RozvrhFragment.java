package gml.waffles.gml;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RozvrhFragment extends Fragment {
    //timetable images
    private static final int[] TIMETABLES = {R.drawable.tridy01, R.drawable.tridy02, R.drawable.tridy03, R.drawable.tridy04, R.drawable.tridy05, R.drawable.tridy06,
            R.drawable.tridy07, R.drawable.tridy08, R.drawable.tridy09, R.drawable.tridy10, R.drawable.tridy11, R.drawable.tridy12, R.drawable.tridy13,
            R.drawable.tridy14, R.drawable.tridy15, R.drawable.tridy16, R.drawable.tridy17, R.drawable.tridy18, R.drawable.tridy19, R.drawable.tridy20,
            R.drawable.tridy21, R.drawable.tridy22, R.drawable.tridy23, R.drawable.tridy24, R.drawable.tridy25, R.drawable.tridy26, R.drawable.tridy27,
            R.drawable.tridy28,};
    //highlight template
    private static final int[][] HIGHLIGHTS = {{R.drawable.tridy_highlight_0_0, R.drawable.tridy_highlight_0_1, R.drawable.tridy_highlight_0_2, R.drawable.tridy_highlight_0_3,
            R.drawable.tridy_highlight_0_4, R.drawable.tridy_highlight_0_5, R.drawable.tridy_highlight_0_6, R.drawable.tridy_highlight_0_7, R.drawable.tridy_highlight_0_8},
            {R.drawable.tridy_highlight_1_0, R.drawable.tridy_highlight_1_1, R.drawable.tridy_highlight_1_2, R.drawable.tridy_highlight_1_3,
                    R.drawable.tridy_highlight_1_4, R.drawable.tridy_highlight_1_5, R.drawable.tridy_highlight_1_6, R.drawable.tridy_highlight_1_7, R.drawable.tridy_highlight_1_8},
            {R.drawable.tridy_highlight_2_0, R.drawable.tridy_highlight_2_1, R.drawable.tridy_highlight_2_2, R.drawable.tridy_highlight_2_3,
                    R.drawable.tridy_highlight_2_4, R.drawable.tridy_highlight_2_5, R.drawable.tridy_highlight_2_6, R.drawable.tridy_highlight_2_7, R.drawable.tridy_highlight_2_8},
            {R.drawable.tridy_highlight_3_0, R.drawable.tridy_highlight_3_1, R.drawable.tridy_highlight_3_2, R.drawable.tridy_highlight_3_3,
                    R.drawable.tridy_highlight_3_4, R.drawable.tridy_highlight_3_5, R.drawable.tridy_highlight_3_6, R.drawable.tridy_highlight_3_7, R.drawable.tridy_highlight_3_8},
            {R.drawable.tridy_highlight_4_0, R.drawable.tridy_highlight_4_1, R.drawable.tridy_highlight_4_2, R.drawable.tridy_highlight_4_3,
                    R.drawable.tridy_highlight_4_4, R.drawable.tridy_highlight_4_5, R.drawable.tridy_highlight_4_6, R.drawable.tridy_highlight_4_7, R.drawable.tridy_highlight_4_8}};
    //image size in pixels (must be set manually)
    private static final int IMAGE_WIDTH = 877;
    private static final int IMAGE_HEIGHT = 620;
    //image size in dpi
    private int imageWidth;
    private int imageHeight;
    private static final long DoubleClickDelay = 300; //delay for double tap (in milliseconds)
    //for hour highlight time
    private static final String[] hoursString = {"07:55", "08:45", "09:40", "10:45", "11:40", "12:35", "13:50", "14:55", "15:45"};
    //mode for touchEvent (0 for none, 1 for drag move, 2 for pinch to zoom) for pinch to zoom and moving
    int mode = 0;
    private View view;
    private SharedPreferences pref; //for saved data
    private ImageView img;
    //screen size
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private float screenWidth;
    private float screenHeight;
    //Matrix for imageView for scaling and moving + backup Matrix
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    //float that represents the scaleFactor
    private float scale;
    //float for default scale value to fit the screen
    private float defaultScale;
    //for double tap to zoom
    private boolean zoomed;
    private long lastClick;
    //values for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    //button for popup
    private Button button;
    //string with class names
    private String tridy[];


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.rozvrh, container, false);

        //set displayMetrics for getting screen size
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = (displayMetrics.heightPixels - getTopBarHeight());
        //get image size in dpi
        float density = displayMetrics.density;
        imageWidth = Math.round(density * IMAGE_WIDTH);
        imageHeight = Math.round(density * IMAGE_HEIGHT);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        pref = getActivity().getPreferences(0);
        //set string of classes
        tridy = getResources().getStringArray(R.array.tridy);
        //button for popup
        button = (Button) view.findViewById(R.id.popupButton);
        //if button is enabled in settings
        if(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("rozvrhButton", true)) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    classPickerDialog(); //show popup
                }
            });
        }else button.setVisibility(View.INVISIBLE);

        img = (ImageView) view.findViewById(R.id.rozvrh);
        setImage(img, button);

        //on click listener for timetable
        img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //prevent viewpager from swiping when zoomed
                if(scale > 1f) view.getParent().requestDisallowInterceptTouchEvent(true);
                else view.getParent().requestDisallowInterceptTouchEvent(false);


                //switch for zoom and move
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        long click = System.currentTimeMillis();
                        if (click - lastClick < DoubleClickDelay) //on double tap
                        {
                            if (scale != defaultScale) zoomed = true;

                            if (zoomed) {
                                setDefaultScale(img, matrix);

                            } else {
                                scale = 1.9f;
                                matrix.postScale(scale, scale, motionEvent.getX(), motionEvent.getY());

                            }
                            zoomed = !zoomed;
                        } else    //on normal tap
                        {
                            savedMatrix.set(matrix);
                            start.set(motionEvent.getX(), motionEvent.getY());
                            mode = 1;
                        }
                        lastClick = click;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(motionEvent);
                        if (oldDist > 10f) {
                            savedMatrix.set(matrix);
                            midPoint(mid, motionEvent);
                            mode = 2;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = 0;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == 1) {
                            matrix.set(savedMatrix);
                            matrix.postTranslate(motionEvent.getX() - start.x, motionEvent.getY() - start.y);
                        } else if (mode == 2) {
                            float newDist = spacing(motionEvent);
                            if (newDist > 10f) {
                                matrix.set(savedMatrix);
                                scale = newDist / oldDist;
                                matrix.postScale(scale, scale, mid.x, mid.y);
                            }
                        }

                        //gets the matrix info
                        float matrixValue[] = new float[9];
                        matrix.getValues(matrixValue);
                        //gets the current scaleFactor
                        float scaleX = matrixValue[Matrix.MSCALE_X];
                        float scaleY = matrixValue[Matrix.MSCALE_Y];
                        //checks if scale factor isnt exceeding limits
                        if (scaleX <= defaultScale) {
                            matrix.postScale((defaultScale) / scaleX, (defaultScale) / scaleY, mid.x, mid.y);
                        } else if (scaleX >= 2.5f) {
                            matrix.postScale((2.5f) / scaleX, (2.5f) / scaleY, mid.x, mid.y);
                        }
                        //checks for position limits
                        adjustPan(matrix, screenWidth, screenHeight, imageHeight, imageWidth);
                        break;
                }
                img.setImageMatrix(matrix);
                return true;
            }
        });
    }

    private void setImage(ImageView img, Button button) {
        int pos = pref.getInt("savedClass", -1); //index of saved image
        if (pos == -1) classPickerDialog();
        else {
            Resources r = getResources();
            //set popup button text
            button.setText(tridy[pos]);
            //if highlight is set in settings
            if(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("rozvrhHighlight", true)) {
                Drawable[] layers = new Drawable[2];
                layers[0] = ResourcesCompat.getDrawable(r, TIMETABLES[pos], null); //set timetable

                //calendar for day index
                Calendar calendar = Calendar.getInstance();
                int dayOfWeek = 0;

                //index of hoursString
                int hourIndex = 0;
                Date time;
                SimpleDateFormat format = new SimpleDateFormat("kk:mm"); //for converting strings to date

                try {
                    time = format.parse(format.format(new Date())); //set current time

                    //for every hour String descending
                    for (int a = hoursString.length - 1; a >= 0; a--) {
                        if (time.before(format.parse(hoursString[a])))
                            hourIndex = a; //set hour index to current hour
                    }
                    //if over last hour highlight next day
                    if (time.after(format.parse(hoursString[hoursString.length - 1]))) dayOfWeek++;

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                switch (calendar.get(Calendar.DAY_OF_WEEK)) //for current day
                {
                    case Calendar.MONDAY:
                        break;
                    case Calendar.TUESDAY:
                        dayOfWeek += 1;
                        break;
                    case Calendar.WEDNESDAY:
                        dayOfWeek += 2;
                        break;
                    case Calendar.THURSDAY:
                        dayOfWeek += 3;
                        break;
                    case Calendar.FRIDAY:
                        dayOfWeek += 4;
                        if (dayOfWeek > 4) dayOfWeek = 0; //condition for friday afternoon
                        break;
                    default: //show 0 0 on weekend
                        dayOfWeek = 0;
                        hourIndex = 0;
                }

                //set highlight image
                Drawable drawable = ResourcesCompat.getDrawable(r, HIGHLIGHTS[dayOfWeek][hourIndex], null);

                //get highlight color from settings
                int color = getResources().getIntArray(R.array.colorCodes)[Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("rozvrhColor", "0"))];
                if (drawable != null) drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY); //color of highlight
                layers[1] = drawable; // set highlight

                LayerDrawable layerDrawable = new LayerDrawable(layers); //combines timetable and highlight
                img.setImageDrawable(layerDrawable); //set image
            }else img.setImageDrawable(ResourcesCompat.getDrawable(r, TIMETABLES[pos], null)); //if not set in settings

            //set default scale and center the image
            setDefaultScale(img, matrix);
        }
    }

    //sets the image to fit the limits
    private void adjustPan(Matrix matrix, float screenWidth, float screenHeight, int imageHeight, int imageWidth) {
        //get variables
        float[] matrixValues = new float[9];
        matrix.getValues(matrixValues); //get matrix info
        float currentY = matrixValues[Matrix.MTRANS_Y]; //get matrix Y
        float currentX = matrixValues[Matrix.MTRANS_X]; //get matrix X
        float currentScale = matrixValues[Matrix.MSCALE_X]; //get current scale of matrix
        float currentHeight = imageHeight * currentScale; //get current height of image
        float currentWidth = imageWidth * currentScale; //get current width of image
        //rectangle representing screen
        RectF displayRect = new RectF();
        displayRect.set(0, 0, screenWidth, screenHeight);
        //rectangle representing image
        RectF drawingRect = new RectF(currentX, currentY, currentX + currentWidth, currentY + currentHeight);
        // variables for spaces between rectangles
        float diffUp = Math.min(displayRect.bottom - drawingRect.bottom, displayRect.top - drawingRect.top);
        float diffDown = Math.max(displayRect.bottom - drawingRect.bottom, displayRect.top - drawingRect.top);
        float diffLeft = Math.min(displayRect.left - drawingRect.left, displayRect.right - drawingRect.right);
        float diffRight = Math.max(displayRect.left - drawingRect.left, displayRect.right - drawingRect.right);
        // position
        float x = 0, y = 0;

        //checks if position doesn't exceed limits
        if (diffUp > 0) //top limit
            y += diffUp;
        if (diffDown < 0) //bottom limit
            y += diffDown;
        if (diffLeft > 0) //left limit
            x += diffLeft;
        if (diffRight < 0) //right limit
            x += diffRight;

        //center the image
        if (currentWidth < displayRect.width())
            x = -currentX + (displayRect.width() - currentWidth) / 2;
        if (currentHeight < displayRect.height())
            y = -currentY + (displayRect.height() - currentHeight) / 2;

        //set position
        matrix.postTranslate(x, y);
    }

    private void setDefaultScale(ImageView img, Matrix matrix) {
        //set default scale
        float widthRatio = (screenWidth) / imageWidth;
        float heightRatio = (screenHeight) / imageHeight;
        scale = widthRatio < heightRatio ? widthRatio : heightRatio;
        defaultScale = scale;
        //set image size to fit the screen
        matrix.setScale(defaultScale, defaultScale);
        //center image on the screen
        adjustPan(matrix, screenWidth, screenHeight, imageHeight, imageWidth);
        //applies all matrix changes
        img.setImageMatrix(matrix);
    }

    //returns height of action + navigation bar
    private int getTopBarHeight() {
        int result = 0;
        //get navigation bar height
        Resources resources = getActivity().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result += resources.getDimensionPixelSize(resourceId);
        }
        //get action bar height
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            result += TypedValue.complexToDimensionPixelSize(tv.data, getActivity().getResources().getDisplayMetrics());
        }
        return result;
    }

    //float for calculating distance between pointers
    private float spacing(MotionEvent motionEvent) {
        float x = motionEvent.getX(0) - motionEvent.getX(1);
        float y = motionEvent.getY(0) - motionEvent.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    //method for calculating and setting focus point of pointers
    private void midPoint(PointF point, MotionEvent motionEvent) {
        float x = motionEvent.getX(0) + motionEvent.getX(1);
        float y = motionEvent.getY(0) + motionEvent.getY(1);
        point.set(x / 2, y / 2);
    }

    //popup for class selection
    private void classPickerDialog() {
        //number picker (scrollbar)
        final NumberPicker picker = new NumberPicker(getActivity());
        picker.setMinValue(0);
        picker.setMaxValue(tridy.length - 1);
        picker.setDisplayedValues(tridy); //sets classes as values (instead of numbers)
        picker.setValue(pref.getInt("savedClass", 0)); //set selected index
        picker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS); //prevents from showing keyboard
        //popup builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(picker);
        builder.setTitle("Vybrat třídu");
        builder.setPositiveButton("Vybrat", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //save selected index
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("savedClass", picker.getValue());
                editor.commit();
                //set image
                setImage(img, button);
            }
        });
        builder.show();
    }
}

