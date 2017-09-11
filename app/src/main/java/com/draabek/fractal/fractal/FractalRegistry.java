package com.draabek.fractal.fractal;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public final class FractalRegistry {
	private static final String LOG_KEY = FractalRegistry.class.getName();
	private static FractalRegistry instance = null;
	private Map<String, Fractal>  fractals;
	private Fractal currentFractal = null;
	private FractalRegistry() {
		fractals = new HashMap<String, Fractal>();
	}
	private boolean initialized = false;

	public static FractalRegistry getInstance() {
		if (instance == null) instance = new FractalRegistry();
		return instance;
	}
	
	public void add(Fractal fractal) {
		fractals.put(fractal.getName(), fractal);
	}
	
	public void remove(Fractal fractal) {
		fractals.remove(fractal.getName());
	}
	
	public Map<String, Fractal> getFractals() {
		return fractals;
	}

	public void init(Context ctx, JsonArray props) {
		if (initialized) return;
		for (JsonElement element : props) {
			JsonObject jsonObject = element.getAsJsonObject();
			String clazz = jsonObject.get("class").getAsString();
            String shaders = jsonObject.get("shaders") != null ? jsonObject.get("shaders").getAsString() : null;
			String name = jsonObject.get("name").getAsString();
			String settingsString = jsonObject.get("settings") != null ?
					jsonObject.get("settings").toString() : null;
			String[] loadedShaders = null;
			Class cls = null;
			try {
                cls = Class.forName(clazz);
				//this is frontal lobotomy
                if (shaders != null) {
					BufferedReader br = new BufferedReader(new InputStreamReader(
							ctx.getAssets().open(shaders + "_fragment.glsl")
					));
					StringBuffer fragmentShader = new StringBuffer();
					String line = null;
					while ((line = br.readLine()) != null) {
						fragmentShader.append(line).append("\n");
					}
					InputStream vertexIS = null;
					try {
						vertexIS = ctx.getAssets().open(shaders + "_vertex.glsl");
					} catch(IOException e) {
						Log.d(LOG_KEY, "Using default vertex shader for fractal " + clazz);
						vertexIS = ctx.getAssets().open("default_vertex.glsl");
					}
					if (vertexIS == null) {//fallback to simplest vertex shader
						Log.e(LOG_KEY, "Not even default vertex shader found for fractal " + clazz);
					}
					br = new BufferedReader(new InputStreamReader(vertexIS));
					StringBuffer vertexShader = new StringBuffer();
					while ((line = br.readLine()) != null) {
						vertexShader.append(line).append("\n");
					}
					loadedShaders = new String[] {vertexShader.toString(), fragmentShader.toString()};
                }
                Fractal fractal = (Fractal)cls.newInstance();
				fractal.setName(name);
				if (fractal instanceof GLSLFractal) {
					((GLSLFractal)fractal).setShaders(loadedShaders);
				}
				if (settingsString != null) {
					Map<String, Float> retMap = new Gson().fromJson(
							settingsString, new TypeToken<HashMap<String, Float>>() {}.getType()
					);
					fractal.updateSettings(retMap);
				}
				add(fractal);
			} catch(ClassNotFoundException e) {
				Log.w(LOG_KEY, "Cannot find fractal class " + clazz);
			} catch(IllegalAccessException e) {
				Log.w(LOG_KEY, "Cannot access fractal class " + clazz);
			} catch(InstantiationException e) {
				//TODO different on debug
				throw new RuntimeException(e);
				//Log.w(LOG_KEY, "Cannot instantiate fractal class " + clazz);
			} catch(IOException e) {
				Log.w(LOG_KEY, "IOException loading fractal " + clazz);
			}
		}
		initialized = true;
	}

	public Fractal get(String name) {
		Fractal f = fractals.get(name);
		if (f != null) return f;
        Log.w(LOG_KEY, "Fractal not found in registry: " + name);
		return null;
	}

	public Fractal getCurrent() {
		return currentFractal;
	}

	public void setCurrent(Fractal currentFractal) {
		this.currentFractal = currentFractal;
	}
}
