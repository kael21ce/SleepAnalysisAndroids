package com.kael21ce.sleepanalysisandroid.data;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class SleepModel {
    static double chi = 45.0, mu = 20.0, mu_s = 0.5;
    static double tau_c = 24.2, alpha_0 = 0.16, beta = 0.013, p = 0.6, i_0 = 9500.0, lambda1 = 60.0;
    static double G = 19.9, b = 0.4, gamma = 0.23, kappa = 12.0 / Math.PI, k = 0.55, f = 0.99669;
    static double coef_y = 0.8, coef_x = -0.16, v_vh = 1.01;

    static double[] PCR_wake(double[] V) {
        double[] dVdt = new double[4];
        dVdt[0] = (1 / kappa) * (gamma * (V[0] - (4 * Math.pow(V[0], 3) / 3)) - V[1] * (Math.pow((24.0 / (f * tau_c)), 2) + k * G * alpha_0 * (1 - V[2]) * (1 - b * V[0]) * (1 - b * V[1]) * Math.pow((250) / i_0, p)));
        dVdt[1] = (1 / kappa) * (V[0] + G * alpha_0 * (1 - V[2]) * (1 - b * V[0]) * (1 - b * V[1]) * Math.pow((250.0) / i_0, p));
        dVdt[2] = lambda1 * (alpha_0 * (Math.pow((250.0) / i_0, p)) * (1 - V[2]) - beta * V[2]);
        dVdt[3] = (1 / chi) * (-V[3] + mu);
        return dVdt;
    }

    static double[] PCR_sleep(double[] V) {
        double[] dVdt = new double[4];
        dVdt[0] = (1 / kappa) * (gamma * (V[0] - (4.0 * Math.pow(V[0], 3) / 3)) - V[1] * (Math.pow((24.0 / (f * tau_c)), 2)));
        dVdt[1] = (1 / kappa) * (V[0]);
        dVdt[2] = lambda1 * (-beta * V[2]);
        dVdt[3] = (1 / chi) * (-V[3] + mu_s);
        return dVdt;
    }

    static double[] rk4_wake(double[] y, double h) {
        int n = y.length;
        double hh = 0.5 * h; // half interval
        double[] yt = new double[n]; // temporary storage

        for (int i = 0; i < n; i++) {
            yt[i] = y[i] + hh * PCR_wake(y)[i];
        } // first step
        double[] dyt = PCR_wake(yt); // get dydx at half time step
        for (int i = 0; i < n; i++) {
            yt[i] = y[i] + hh * dyt[i];
        } // second step
        double[] dym = PCR_wake(yt); // get updated dydx at half time step
        for (int i = 0; i < n; i++) {
            yt[i] = y[i] + h * dym[i]; // third step
            dym[i] = dyt[i] + dym[i];
        }
        dyt = PCR_wake(yt); // fourth step
        for (int i = 0; i < n; i++) {
            yt[i] = y[i] + h * (PCR_wake(y)[i] + dyt[i] + 2.0 * dym[i]) / 6.0;
        }   // do final step w/ appropriate weights
        return yt;   // return updated y[] at x+h
    }

    static double[] rk4_sleep(double[] y, double h) {
        int n = y.length;
        double hh = 0.5 * h; // half interval
        double[] yt = new double[n]; // temporary storage

        for (int i = 0; i < n; i++) {
            yt[i] = y[i] + hh * PCR_sleep(y)[i];
        } // first step
        double[] dyt = PCR_sleep(yt); // get dydx at half time step
        for (int i = 0; i < n; i++) {
            yt[i] = y[i] + hh * dyt[i];
        } // second step
        double[] dym = PCR_sleep(yt); // get updated dydx at half time step
        for (int i = 0; i < n; i++) {
            yt[i] = y[i] + h * dym[i]; // third step
            dym[i] = dyt[i] + dym[i];
        }
        dyt = PCR_sleep(yt); // fourth step
        for (int i = 0; i < n; i++) {
            yt[i] = y[i] + h * (PCR_sleep(y)[i] + dyt[i] + 2.0 * dym[i]) / 6.0;
        }   // do final step w/ appropriate weights
        return yt;   // return updated y[] at x+h
    }

    public static ArrayList<double[]> pcr_simulation(double[] V0, double[] sleep_pattern, double h) {
        int duration = sleep_pattern.length;
        ArrayList<double[]> y = new ArrayList<>();
        double[] V_tmp = V0;
        y.add(V0);
        for (int i = 0; i < duration - 1; i++) { //simulate every 1 min range
            //if model simulation and sleep pattern do not match
            if (sleep_pattern[i + 1] > 0.5) { //forced sleep case
                V_tmp = rk4_sleep(V_tmp, h);
            } else { //normal wake case
                V_tmp = rk4_wake(V_tmp, h);
            }
            y.add(V_tmp); //record the model simulation result
        }
        return y; //return the simulation result
    }

    static double[] pcr_simulation_end(double[] V0, double[] sleep_pattern, double h) {
        int duration = sleep_pattern.length;
        double[] V_tmp = V0;
        for (int i = 0; i < duration-1; i++) { //simulate every 1 min range
            //if model simulation and sleep pattern do not match
            if (sleep_pattern[i + 1] > 0.5) { //forced sleep case
                V_tmp = rk4_sleep(V_tmp, h);
            } else { //normal wake case
                V_tmp = rk4_wake(V_tmp, h);
            }
        }
        return V_tmp; //return the simulation result
    }

    static ArrayList<double[]> init_data() {
        double[] V0 = {-0.8590, -0.6837, 0.1140, 14.2133}; //initial condition
        double[] sleep_pattern = new double[12 * 24 * 3];
        Arrays.fill(sleep_pattern, 0);

        for (int i = 1; i < 4; i++) { //sleep from 1h to 7h for every day
            for (int j = (288 * (i - 1) + 12); j <= (288 * (i - 1) + 7 * 12); j++) {
                sleep_pattern[j] = 1.0;
            }
        }

        return pcr_simulation(V0, sleep_pattern, 5 / 60.0); // run the simulation
    }

    //type1: sleep is enough
    //type2: sleep is early
    //-> 0: false, 1: true
    public static int[] Sleep_pattern_suggestion(double[] V0, int sleep_onset, int work_onset, int work_offset, double step) {
        int buffer = (int) Math.round(1 / step); // Time between nap offset and work onset
        int unit = (int) Math.round(0.5 / step);
        int len0 = work_onset - sleep_onset; // length between work onset and work onset
        int len1 = work_offset - work_onset;
        int type1 = 0, type2 = 0;
        int[] result = {0, 0, 0, 0, 0, 0}; //Output 1
        int i, idx; // iterator

        if (sleep_onset <= 0 || len0 <= buffer || len1 <= 0) { // Wrong input
            result[4] = 0;
            result[5] = 0;
            return result;
        }

        // Find CSS sleep
        double[] sleep_pattern1 = new double[sleep_onset + 1];
        double[] V_tmp0 = pcr_simulation_end(V0, sleep_pattern1, step);
        double[] V_tmp = V_tmp0;
        double H = V_tmp[3];// sleep pressure
        double D_up = (2.46 + 10.2 + (3.37 * 0.5) * (1.0 + coef_y * V_tmp[1] + coef_x * V_tmp[0])) / v_vh; // sleep threshold

        int sleep_start = 0, sleep_amount = 0; // first point where the CSS sleep is possible
        type2 = 0;

        if (D_up > H) { // CSS sleep is impossible at sleep onset -> Need more awake
            type2 = 1;
            i = 0;
            while (i < (len0 - buffer)) { // find the point where the CSS sleep is possible
                V_tmp = rk4_wake(V_tmp, step); // simulate awake
                H = V_tmp[3];
                D_up = (2.46 + 10.2 + (3.37 * 0.5) * (1.0 + coef_y * V_tmp[1] + coef_x * V_tmp[0])) / v_vh;
                i = i + 1;
                if (D_up < H) {
                    break;
                }
            }

            if (i >= len0) { //CSS sleep is impossible before the work onset
                V_tmp = V_tmp0;
            }
            else{
                sleep_start = i; // the earliest time that CSS sleep is possible
                i = 0;
                while (D_up < H) {
                    i = i + 1;
                    V_tmp = rk4_sleep(V_tmp, step);
                    H = V_tmp[3];
                    D_up = (2.46 + 10.2 + (3.37 * 0.5) * (1.0 + coef_y * V_tmp[1] + coef_x * V_tmp[0]))/v_vh;
                }
                sleep_amount = i;
            }
        } else { // CSS sleep is possible at the sleep onset
            i = 0;
            while (D_up < H) {
                i = i + 1;
                V_tmp = rk4_sleep(V_tmp, step);
                H = V_tmp[3];
                D_up = (2.46 + 10.2 + (3.37 * 0.5) * (1.0 + coef_y * V_tmp[1] + coef_x * V_tmp[0]))/v_vh;
            }
            sleep_amount = i;
        }
        int CSS_start = 0, CSS_end = 0;
        if (sleep_start + sleep_amount >= (len0 - buffer)) {
            result[0] = sleep_onset + sleep_start;
            result[1] = work_onset - buffer;
            result[4] = 0;
            result[5] = type2;
            return result;
        }
        if (sleep_amount < unit) { // CSS sleep is impossible or meaningless
            CSS_end = sleep_onset; // start of CSS sleep
            V_tmp = V_tmp0;
        }
        else{ // CSS sleep is identified
            CSS_start = sleep_onset + sleep_start; // start of CSS sleep
            CSS_end = CSS_start + sleep_amount; // end of CSS sleep
            result[0]  = CSS_start;
            result[1] = CSS_end;
        }

        sleep_pattern1 = new double[work_offset - CSS_end + 1];
        // print(work_offset, CSS)
        ArrayList<double[]> y_temp = pcr_simulation(V_tmp, sleep_pattern1, step);

        double C1;
        double H1 = 0.0, D_up1 = 0.0;
        for (int j = work_onset; j <= work_offset; j++) {
            H1 += y_temp.get(j - CSS_end)[3];
            C1 = (3.37 * 0.5) * (1.0 + coef_y * y_temp.get(j - CSS_end)[1] + coef_x * y_temp.get(j - CSS_end)[0]);
            D_up1 += (2.46 + 10.2 + C1)/v_vh;
        }

        if (D_up1 - H1 > 0) { //AL condition is satisfied
            result[4] = 1;
            result[5] = type2;
            return result;
        }

        i = 1;
        ArrayList<double[]> y_temp2;
        while (true) { // simulate repeatedly increasing nap duration
            if (CSS_end >= (work_onset - unit * i - buffer))
            { // AL condition is not satisfied even we take full sleep
                result[0] = sleep_onset;
                result[1] = work_onset - buffer;
                result[4] = 0;
                result[5] = type2;
                return result;
            }

            V_tmp = y_temp.get(work_onset - i * unit - buffer - CSS_end);
            sleep_pattern1 = new double[len1 + i * unit + buffer + 1];
            for (int j = 0; j < i*unit; j++) {
                sleep_pattern1[j] = 1.0; // simulate increasing nap as 30 min
            }
            y_temp2 = pcr_simulation(V_tmp, sleep_pattern1, step);
            // simulate from the end of CSS sleep to the end of work with nap

            H1 = 0.0;
            D_up1 = 0.0;
            for (int j = work_onset; j <= work_offset; j++) {
                idx = j - work_onset + i * unit + buffer;
                H1 += y_temp2.get(idx)[3];
                C1 = (3.37 * 0.5) * (1.0 + coef_y * y_temp2.get(idx)[1]
                        + coef_x * y_temp2.get(idx)[0]);
                D_up1 += (2.46 + 10.2 + C1)/v_vh;
            }

            if (D_up1 - H1 > 0) { //AL condition is satisfied
                break;
            }
            i = i + 1;
        }
        result[2] = work_onset - i * unit - buffer;
        result[3] = work_onset - buffer; // just in case of numerical error, add 30 min sleep

        if (result[2] - result[1] < buffer){
            result[1] = result[1] + result[3] - result[2];
        }
        result[4] = 1;
        result[5] = type2;
        return result;
    }
}
