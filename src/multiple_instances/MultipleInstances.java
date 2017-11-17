package multiple_instances;


import com.jogamp.newt.event.*;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_HIGH;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_MEDIUM;
import static com.jogamp.opengl.GL2ES3.*;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL4.GL_MAP_COHERENT_BIT;
import static com.jogamp.opengl.GL4.GL_MAP_PERSISTENT_BIT;

/*
* This class is loosly based on the code found at the following link
* https://github.com/java-opengl-labs/hello-triangle/blob/master/src/main/java/gl4/HelloTriangleSimple.java
* 
* The class creates a window capable of rendering graphics using OpenGL 4
*/
public class MultipleInstances implements GLEventListener, KeyListener {

    // OpenGL window reference
    private static GLWindow window;

    // The animator is responsible for continuous operation
    private static Animator animator;

    // The program entry point
    public static void main(String[] args) {
        new MultipleInstances().setup();
    }
    
    // Initial camera position
    private float[]cameraPos = {0f, -2f, 0f };

    // Vertex data cube
    private float[] vertexDataCube = {
        // Front
        -1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, 1.0f, 0.0f, 0.0f,
        1.0f, -1.0f, 1.0f, 1.0f, 0.0f,
        1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
        // Back
        1.0f, 1.0f, -1.0f, 0.0f, 1.0f,
        1.0f, -1.0f, -1.0f, 0.0f, 0.0f,
        -1.0f, -1.0f, -1.0f, 1.0f, 0.0f,
        -1.0f, 1.0f, -1.0f, 1.0f, 1.0f,
        // Left
        -1.0f, 1.0f, -1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, -1.0f, 0.0f, 0.0f,
        -1.0f, -1.0f, 1.0f, 1.0f, 0.0f,
        -1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
        // Right
        1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
        1.0f, -1.0f, 1.0f, 0.0f, 0.0f,
        1.0f, -1.0f, -1.0f, 1.0f, 0.0f,
        1.0f, 1.0f, -1.0f, 1.0f, 1.0f,
        // Top
        -1.0f, 1.0f, -1.0f, 0.0f, 1.0f,
        -1.0f, 1.0f, 1.0f, 0.0f, 0.0f,
        1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
        1.0f, 1.0f, -1.0f, 1.0f, 1.0f,
        // Bottom
        -1.0f, -1.0f, 1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, -1.0f, 0.0f, 0.0f,
        1.0f, -1.0f, -1.0f, 1.0f, 0.0f,
        1.0f, -1.0f, 1.0f,  1.0f, 1.0f
    };

    // Triangles
    private short[] elementDataCube = {
        // Front
        0, 1, 2, 2, 3, 0,
        // Back
        4, 5, 6, 6, 7, 4,
        // Left
        8, 9, 10, 10, 11, 8,
        // Right
        12, 13, 14, 14, 15, 12,
        // Top
        16, 17, 18, 18, 19, 16,
        // Bottom
        20, 21, 22, 22, 23, 20
    };
    
    private float[] vertexDataPyramid = {
            // Bottom
            -1.0f, -1.0f, 1.0f, 0.0f, 10.0f, 	
            -1.0f, -1.0f, -1.0f, 0.0f, 0.0f,	
            1.0f, -1.0f, -1.0f, 10.0f, 0.0f,		
            1.0f, -1.0f, 1.0f,  10.0f, 10.0f,		
            
    		// Front
    		0.0f, 1.0f, 0.0f, 0.0f, 10.0f,		
            -1.0f, -1.0f, 1.0f, 0.0f, 0.0f, 	
            1.0f, -1.0f, 1.0f, 10.0f, 0.0f, 		

            // Right
            0.0f, 1.0f, 0.0f, 0.0f, 10.0f,	
            1.0f, -1.0f, 1.0f, 0.0f, 0.0f,	
            1.0f, -1.0f, -1.0f, 10.0f, 0.0f,		
            
            // Back
            0.0f, 1.0f, 0.0f, 0.0f, 10.0f,		
            1.0f, -1.0f, -1.0f, 0.0f, 0.0f,		
            -1.0f, -1.0f, -1.0f, 10.0f, 0.0f,	
            
            // Left
            0.0f, 1.0f, 0.0f, 0.0f, 10.0f,		
            -1.0f, -1.0f, -1.0f, 0.0f, 0.0f,	
            -1.0f, -1.0f, 1.0f, 10.0f, 0.0f, 	
    };
    private short[] elementDataPyramid = {
    		// Bottom
    	    0, 1, 2, 2, 0, 3, 
    	    // Pyramid
    	    6, 4, 5, 15, 13, 14, 12, 10, 11, 9, 7, 8
    };
    
    // Interface for creating final static variables for defining the buffers
    private interface Buffer {
        int VERTEX = 0;
        int ELEMENT = 1;
        int GLOBAL_MATRICES = 2;
        int MODEL_MATRIX1 = 3;
        int MODEL_MATRIX2 = 4;
        int MODEL_MATRIX3 = 5;
        int MODEL_MATRIX_PYRAMID = 6;
        int VERTEX_PYRAMID = 7;
        int ELEMENT_PYRAMID = 8;
        int MODEL_MATRIX4 = 9;
        int MODEL_MATRIX5 = 10;
        int MODEL_MATRIX6 = 11;
        int MODEL_MATRIX7 = 12;
        int MODEL_MATRIX8 = 13;
        int MODEL_MATRIX9 = 14;
        int MAX = 15;
    }

    // The OpenGL profile
    GLProfile glProfile;

    // The texture filename 
    private final String textureFilename = "src/multiple_instances/cactus.png";
    private final String textureSand = "src/multiple_instances/sand.png";
    private final String texturePyramid = "src/multiple_instances/sandbrick.png";
    private final String textureMummy = "src/multiple_instances/mummy.png";
    private final String textureSky = "src/multiple_instances/sky.png";
    private final String textureWay = "src/multiple_instances/way.png";
    private final String textureTornado = "src/multiple_instances/tornado.png";


    // Create buffers for the names
    private IntBuffer bufferNames = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(2);
    private IntBuffer textureNames = GLBuffers.newDirectIntBuffer(7);

    // Create buffers for clear values
    private FloatBuffer clearColor = GLBuffers.newDirectFloatBuffer(new float[] {0, 0, 0, 0});
    private FloatBuffer clearDepth = GLBuffers.newDirectFloatBuffer(new float[] {1});

    // Create references to buffers for holding the matrices
    private ByteBuffer 
    globalMatricesPointer, 
    modelMatrixPointer1, 
    modelMatrixPointer2, 
    modelMatrixPointer3, 
    modelMatrixPointerPyramid, 
    modelMatrixPointer4, 
    modelMatrixPointer5, 
    modelMatrixPointer6,
    modelMatrixPointer7,
    modelMatrixPointer8,
    modelMatrixPointer9
    ;
    

    // https://jogamp.org/bugzilla/show_bug.cgi?id=1287
    private boolean bug1287 = true;

    // Program instance reference
    private Program program;

    // Variable for storing the start time of the application
    private long start;


    // Application setup function
    private void setup() {

        // Get a OpenGL 4.x profile (x >= 0)
        glProfile = GLProfile.get(GLProfile.GL4);

        // Get a structure for definining the OpenGL capabilities with default values
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);

        // Create the window with default capabilities
        window = GLWindow.create(glCapabilities);

        // Set the title of the window
        window.setTitle("Assignment 1 - Vlad Jakovlev");

        // Set the size of the window
        window.setSize(1920, 1024);

        // Set debug context (must be set before the window is set to visible)
        window.setContextCreationFlags(GLContext.CTX_OPTION_DEBUG);

        // Make the window visible
        window.setVisible(true);

        // Add OpenGL and keyboard event listeners
        window.addGLEventListener(this);
        window.addKeyListener(this);

        // Create and start the animator
        animator = new Animator(window);
        animator.start();

        // Add window event listener
        window.addWindowListener(new WindowAdapter() {
            // Window has been destroyed
            @Override
            public void windowDestroyed(WindowEvent e) {
                // Stop animator and exit
                animator.stop();
                System.exit(1);
            }
        });
    }


    // GLEventListener.init implementation
    @Override
    public void init(GLAutoDrawable drawable) {

        // Get OpenGL 4 reference
        GL4 gl = drawable.getGL().getGL4();

        // Initialize debugging
        initDebug(gl);

        // Initialize buffers
        initBuffers(gl);

        // Initialize vertex array
        initVertexArray(gl);

        // Initialize texture
        initTexture(gl);

        // Set up the program
        program = new Program(gl, "multiple_instances", "multiple_instances", "multiple_instances");

        // Enable Opengl depth buffer testing
        gl.glEnable(GL_DEPTH_TEST);

        // Store the starting time of the application
        start = System.currentTimeMillis();
    }

    // GLEventListener.display implementation
    @Override
    public void display(GLAutoDrawable drawable) {

        // Get OpenGL 4 reference
        GL4 gl = drawable.getGL().getGL4();


        // Copy the view matrix to the server
        {
            // Create identity matrix
            float[] view = FloatUtil.makeTranslation(new float[16], 0, false, cameraPos[0], cameraPos[1], cameraPos[2]);
            // Copy each of the values to the second of the two global matrices
            for (int i = 0; i < 16; i++)
                globalMatricesPointer.putFloat(16 * 4 + i * 4, view[i]);
        }


        // Clear the color and depth buffers
        gl.glClearBufferfv(GL_COLOR, 0, clearColor);
        gl.glClearBufferfv(GL_DEPTH, 0, clearDepth);

        // Activate the vertex program and vertex array
        gl.glUseProgram(program.name);
        gl.glBindVertexArray(vertexArrayName.get(0));
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(0));

        // Bind the global matrices buffer to a specified index within the uniform buffers
        //cube
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM0,
                bufferNames.get(Buffer.GLOBAL_MATRICES));
        // Copy the model matrix to the server
        {
            // Find a time delta for the time passed since the start of execution
            long now = System.currentTimeMillis();
            float diff = (float) (now - start) / 1_00;
            
            
            // Create a scale matrix 
            float[] scale = FloatUtil.makeScale(new float[16], true, 0.5f, 2f, 0.5f);
            float[] scaleTop = FloatUtil.makeScale(new float[16], true, 2.5f, 0.5f, 0.5f);
            float[] scaleSand = FloatUtil.makeScale(new float[16], true, 30f, 0.01f, 30f);
            float[] scalePyramid = FloatUtil.makeScale(new float[16], true, 15f, 15f, 15f);
            float[] scaleMummy = FloatUtil.makeScale(new float[16], true, 2f, 2f, 0.5f);
            float[] scaleSky = FloatUtil.makeScale(new float[16], true, 30f, 30f, 30f);
            float[] scaleCactus = FloatUtil.makeScale(new float[16], true, 0.5f, 2f, 0.25f);
            float[] scaleWay = FloatUtil.makeScale(new float[16], true, 2f, 0.02f, 20f);
            float[] scaleTornado = FloatUtil.makeScale(new float[16], true, 5f, 20f, 3f);



            // Create a translation matrix 
            float[] translateDoorLeft = FloatUtil.makeTranslation(new float[16], 0, true, -4f, 0.6f, -50.5f);
            float[] translateDoorRight = FloatUtil.makeTranslation(new float[16], 0, true, 4f, 0.6f, -50.5f);
            float[] translateDoorTop = FloatUtil.makeTranslation(new float[16], 0, true, 0f, 7.40f, -50.5f);
            float[] translateSand = FloatUtil.makeTranslation(new float[16], 0, true, 0f, -80f, -1f);
            float[] translatePyramid = FloatUtil.makeTranslation(new float[16], 0, true, 0f, 0.95f, -2.67f);
            float[] translateMummy = FloatUtil.makeTranslation(new float[16], 0, true, 0f, 0.6f, -50.5f);
            float[] translateSky = FloatUtil.makeTranslation(new float[16], 0, true, 0f, 0.95f, -1f);
            float[] translateCactus = FloatUtil.makeTranslation(new float[16], 0, true, 20f, 0.6f, -45f);
            float[] translateWay = FloatUtil.makeTranslation(new float[16], 0, true, 0f, -37f, -1f);
            float[] translateTornado = FloatUtil.makeTranslation(new float[16], 0, true, -3f, 0.97f, -3f);


            // Create a rotation matrix around the z axis based on the time delta
            float[] rotate = FloatUtil.makeRotationAxis(new float[16], 0, diff, 0f, 1f, 0f, new float[3]);
            float[] reverse = FloatUtil.makeRotationAxis(new float[16], 0, (float)3.140, 0f, 0f, 1f, new float[3]);
            float[] rotate2 = FloatUtil.makeRotationAxis(new float[16], 0, 0, 0f, 1f, 0f, new float[3]);
            float[] static3 = FloatUtil.makeRotationAxis(new float[16], 0, 0, 0f, 0f, 0f, new float[3]);
            
            // Combine the three matrices by multiplying them
            float[] modelDoorLeft = FloatUtil.multMatrix(FloatUtil.multMatrix(scale, translateDoorLeft, new float[16]), static3, new float[16]);
            float[] modelDoorRight = FloatUtil.multMatrix(FloatUtil.multMatrix(scale, translateDoorRight, new float[16]), rotate2, new float[16]);
            float[] modelFloorSand = FloatUtil.multMatrix(FloatUtil.multMatrix(scaleSand, translateSand, new float[16]), static3, new float[16]);
            float[] modelDoorTop = FloatUtil.multMatrix(FloatUtil.multMatrix(scaleTop, translateDoorTop, new float[16]), rotate2, new float[16]);
            float[] modelPyramid = FloatUtil.multMatrix(FloatUtil.multMatrix(scalePyramid, translatePyramid, new float[16]), static3, new float[16]);
            float[] modelMummy = FloatUtil.multMatrix(FloatUtil.multMatrix(scaleMummy, translateMummy, new float[16]), static3, new float[16]);
            float[] modelSky = FloatUtil.multMatrix(FloatUtil.multMatrix(scaleSky, translateSky, new float[16]), static3, new float[16]);
            float[] modelCactus = FloatUtil.multMatrix(FloatUtil.multMatrix(scaleCactus, translateCactus, new float[16]), static3, new float[16]);
            float[] modelWay = FloatUtil.multMatrix(FloatUtil.multMatrix(scaleWay, translateWay, new float[16]), static3, new float[16]);
            float[] modelTornado = FloatUtil.multMatrix(FloatUtil.multMatrix(scaleTornado, translateTornado, new float[16]), FloatUtil.multMatrix(rotate, reverse, new float[16]));

            // Copy the entire matrix to the server
            modelMatrixPointer1.asFloatBuffer().put(modelDoorLeft);
            modelMatrixPointer2.asFloatBuffer().put(modelDoorRight);
            modelMatrixPointer3.asFloatBuffer().put(modelFloorSand);
            modelMatrixPointerPyramid.asFloatBuffer().put(modelPyramid);
            modelMatrixPointer4.asFloatBuffer().put(modelDoorTop);
            modelMatrixPointer5.asFloatBuffer().put(modelMummy);
            modelMatrixPointer6.asFloatBuffer().put(modelSky);
            modelMatrixPointer7.asFloatBuffer().put(modelCactus);
            modelMatrixPointer8.asFloatBuffer().put(modelWay);
            modelMatrixPointer9.asFloatBuffer().put(modelTornado);
            
        }
        ///////////////////////////////////////////////////////////////////////////////
        //door
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(2));
        // Bind the model matrix buffer to a specified index within the uniform buffers
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX1));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);

        // Bind the model matrix buffer to a specified index within the uniform buffers
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX2));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX4));
        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);
        
        /////////////////////////////////////////////////////////////////////////////////////
        
        //floor
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(1));
        // Bind the model matrix buffer to a specified index within the uniform buffers
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX3));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);
        
        ////////////////////////////////////////////////////////////////////////////////////
        // pyramid draw
        gl.glBindVertexArray(vertexArrayName.get(1));
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(2));
        // Bind the model matrix buffer to a specified index within the uniform buffers
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX_PYRAMID));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataPyramid.length,
                GL_UNSIGNED_SHORT,
                0);
        ////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////
        //sky
        gl.glBindVertexArray(vertexArrayName.get(0));
       gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(4));
        // Draw the triangle
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX6));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);
        
        //////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////
        //mummy
        gl.glBindVertexArray(vertexArrayName.get(0));
       gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(3));
        // Draw the triangle
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX5));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);
        
        //////////////////////////////////////////////////////////////////////////////
        
        /////////////////////////////////////////////////////////////////////////////////
        //cactus
        gl.glBindVertexArray(vertexArrayName.get(0));
       gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(0));
        // Draw the triangle
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX7));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);
        
        //////////////////////////////////////////////////////////////////////////////
        
        /////////////////////////////////////////////////////////////////////////////////
        //way
        gl.glBindVertexArray(vertexArrayName.get(0));
       gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(5));
        // Draw the triangle
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX8));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);
        
        //////////////////////////////////////////////////////////////////////////////
        
        /////////////////////////////////////////////////////////////////////////////////
        //Tornado
        gl.glBindVertexArray(vertexArrayName.get(1));
       gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(6));
        // Draw the triangle
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX9));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);
        
        //////////////////////////////////////////////////////////////////////////////
        
        // Deactivate the program and vertex array
        gl.glUseProgram(0);
        gl.glBindVertexArray(0);
        gl.glBindTexture(gl.GL_TEXTURE_2D, 0);
    }

    // GLEventListener.reshape implementation
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        // Get OpenGL 4 reference
        GL4 gl = drawable.getGL().getGL4();

        // Create an orthogonal projection matrix 
        float[] ortho = FloatUtil.makePerspective(new float[16], 0, false, (float)Math.PI/2f, (float)width/height, 0.1f, 100f);
		
        // Copy the projection matrix to the server
        globalMatricesPointer.asFloatBuffer().put(ortho);

        // Set the OpenGL viewport
        gl.glViewport(x, y, width, height);
    }

    // GLEventListener.dispose implementation
    @Override
    public void dispose(GLAutoDrawable drawable) {

        // Get OpenGL 4 reference
        GL4 gl = drawable.getGL().getGL4();

        // Unmap the transformation matrices
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.GLOBAL_MATRICES));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX1));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX2));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX3));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX_PYRAMID));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX4));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX5));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX6));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX7));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX8));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX9));

        // Delete the program
        gl.glDeleteProgram(program.name);

        // Delete the vertex array
        gl.glDeleteVertexArrays(2, vertexArrayName);

        // Delete the buffers
        gl.glDeleteBuffers(Buffer.MAX, bufferNames);
        
        gl.glDeleteTextures(5, textureNames);
    }

    // KeyListener.keyPressed implementation
    @Override
    public void keyPressed(KeyEvent e) {
        // Destroy the window if the escape key is pressed
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            new Thread(() -> {
                window.destroy();
            }).start();
        }
        
        //key WASD controls for cameraPos
        //W key, z axis 
        if(e.getKeyCode() == KeyEvent.VK_W){
        	new Thread(() -> {
        		cameraPos[2] = (float) (cameraPos[2]+0.5);
        	}).start();
        };
        //S key, z axis
        if(e.getKeyCode() == KeyEvent.VK_S){
        	new Thread(() -> {
        		cameraPos[2] = (float) (cameraPos[2]-0.5);
        	}).start();
        };
        //A key, x axis
        if(e.getKeyCode() == KeyEvent.VK_A){
        	new Thread(() -> {
        		cameraPos[0] = (float) (cameraPos[0]+0.5);
        	}).start();
        };
        //D key, x axis
        if(e.getKeyCode() == KeyEvent.VK_D){
        	new Thread(() -> {
        		cameraPos[0] = (float) (cameraPos[0]-0.5);
        	}).start();
        };
        // Up arrow key, Y axis
        if(e.getKeyCode() == KeyEvent.VK_UP){
        	new Thread(() -> {
        		cameraPos[1] = (float) (cameraPos[1]-0.5);
        	}).start();
        };
        // Down arrow key, Y axis
        if(e.getKeyCode() == KeyEvent.VK_DOWN){
        	new Thread(() -> {
        		cameraPos[1] = (float) (cameraPos[1]+0.5);
        	}).start();
        };
        
    }

    // KeyListener.keyPressed implementation
    @Override
    public void keyReleased(KeyEvent e) {
    }

    // Function for initializing OpenGL debugging
    private void initDebug(GL4 gl) {

        // Register a new debug listener
        window.getContext().addGLDebugListener(new GLDebugListener() {
            // Output any messages to standard out
            @Override
            public void messageSent(GLDebugMessage event) {
                System.out.println(event);
            }
        });

        // Ignore all messages
        gl.glDebugMessageControl(
                GL_DONT_CARE,
                GL_DONT_CARE,
                GL_DONT_CARE,
                0,
                null,
                false);

        // Enable messages of high severity
        gl.glDebugMessageControl(
                GL_DONT_CARE,
                GL_DONT_CARE,
                GL_DEBUG_SEVERITY_HIGH,
                0,
                null,
                true);

        // Enable messages of medium severity
        gl.glDebugMessageControl(
                GL_DONT_CARE,
                GL_DONT_CARE,
                GL_DEBUG_SEVERITY_MEDIUM,
                0,
                null,
                true);
    }

    // Function fo initializing OpenGL buffers
    private void initBuffers(GL4 gl) {

        // Create a new float direct buffer for the vertex data cube
        FloatBuffer vertexBufferCube = GLBuffers.newDirectFloatBuffer(vertexDataCube);
        // Create a new float direct buffer for the vertex data cube
        FloatBuffer vertexBufferPyramid = GLBuffers.newDirectFloatBuffer(vertexDataPyramid);

        // Create a new short direct buffer for the triangle indices cube
        ShortBuffer elementBufferCube = GLBuffers.newDirectShortBuffer(elementDataCube);
        
        // Create a new short direct buffer for the triangle indices pyramid
        ShortBuffer elementBufferPyramid = GLBuffers.newDirectShortBuffer(elementDataPyramid);
        
        // Create the OpenGL buffers
        gl.glCreateBuffers(Buffer.MAX, bufferNames);

        // If the workaround for bug 1287 isn't needed
        if (!bug1287) {

            // Create and initialize a named buffer storage for the vertex data
        	//cube
            gl.glNamedBufferStorage(bufferNames.get(Buffer.VERTEX), vertexBufferCube.capacity() * Float.BYTES, vertexBufferCube, GL_STATIC_DRAW);
            //pyramid
            gl.glNamedBufferStorage(bufferNames.get(Buffer.VERTEX_PYRAMID), vertexBufferPyramid.capacity() * Float.BYTES, vertexBufferPyramid, GL_STATIC_DRAW);
            

            // Create and initialize a named buffer storage for the triangle indices
            //cube
            gl.glNamedBufferStorage(bufferNames.get(Buffer.ELEMENT), elementBufferCube.capacity() * Short.BYTES, elementBufferCube, GL_STATIC_DRAW);
            //pyramid
            // Create and initialize a named buffer storage for the triangle indices
            gl.glNamedBufferStorage(bufferNames.get(Buffer.ELEMENT_PYRAMID), elementBufferPyramid.capacity() * Short.BYTES, elementBufferPyramid, GL_STATIC_DRAW);


            // Create and initialize a named buffer storage for the global and model matrices 
            gl.glNamedBufferStorage(bufferNames.get(Buffer.GLOBAL_MATRICES), 16 * 4 * 2, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX1), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX2), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX3), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX_PYRAMID), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX4), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX5), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX6), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX7), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX8), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX9), 16 * 4, null, GL_MAP_WRITE_BIT);


        } else {

            // Create and initialize a buffer storage for the vertex data
        	//cube
            gl.glBindBuffer(GL_ARRAY_BUFFER, bufferNames.get(Buffer.VERTEX));
            gl.glBufferStorage(GL_ARRAY_BUFFER, vertexBufferCube.capacity() * Float.BYTES, vertexBufferCube, 0);
            gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
            //pyramid
            gl.glBindBuffer(GL_ARRAY_BUFFER, bufferNames.get(Buffer.VERTEX_PYRAMID));
            gl.glBufferStorage(GL_ARRAY_BUFFER, vertexBufferPyramid.capacity() * Float.BYTES, vertexBufferPyramid, 0);
            gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
            
            // Create and initialize a buffer storage for the triangle indices 
            //cube
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferNames.get(Buffer.ELEMENT));
            gl.glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, elementBufferCube.capacity() * Short.BYTES, elementBufferCube, 0);
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            //pyramid
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferNames.get(Buffer.ELEMENT_PYRAMID));
            gl.glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, elementBufferPyramid.capacity() * Short.BYTES, elementBufferPyramid, 0);
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            


            // Retrieve the uniform buffer offset alignment minimum
            IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);
            gl.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset);
            // Set the required bytes for the matrices in accordance to the uniform buffer offset alignment minimum
            int globalBlockSize = Math.max(16 * 4 * 2, uniformBufferOffset.get(0));
            int modelBlockSize = Math.max(16 * 4, uniformBufferOffset.get(0));

            
            // Create and initialize a named storage for the global matrices 
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.GLOBAL_MATRICES));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, globalBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);

            // Create and initialize a named storage for the model matrices 
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX1));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX2));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX3));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            //Pyramid
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX_PYRAMID));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            //top door
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX4));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            //mummy
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX5));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            //SKY
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX6));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            //cactus
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX7));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
          //way
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX8));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
          //tornado
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX9));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        // map the global matrices buffer into the client space
        globalMatricesPointer = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.GLOBAL_MATRICES),
                0,
                16 * 4 * 2,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT); // flags

        // map the model matrix buffer into the client space
        modelMatrixPointer1 = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX1),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        modelMatrixPointer2 = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX2),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        modelMatrixPointer3 = (gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX3),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT));
        modelMatrixPointerPyramid = (gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX_PYRAMID),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT));
        modelMatrixPointer4 = (gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX4),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT));
        modelMatrixPointer5 = (gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX5),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT));
        modelMatrixPointer6 = (gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX6),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT));
        modelMatrixPointer7 = (gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX7),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT));
        modelMatrixPointer8 = (gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX8),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT));
        modelMatrixPointer9 = (gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX9),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT));
    }

    // Function for initializing the vertex array
    private void initVertexArray(GL4 gl) {

        // Create a single vertex array object
        gl.glCreateVertexArrays(2, vertexArrayName);

        // Associate the vertex attributes in the vertex array object with the vertex buffer
        //cube
        gl.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.POSITION, Semantic.Stream.A);
        gl.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.TEXCOORD, Semantic.Stream.A);
        //pyramid
        gl.glVertexArrayAttribBinding(vertexArrayName.get(1), Semantic.Attr.POSITION, Semantic.Stream.A);
        gl.glVertexArrayAttribBinding(vertexArrayName.get(1), Semantic.Attr.TEXCOORD, Semantic.Stream.A);
    
        // Set the format of the vertex attributes in the vertex array object
        //cube
        gl.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.POSITION, 3, GL_FLOAT, false, 0);
        gl.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 3 * 4);
        //pyramid
        gl.glVertexArrayAttribFormat(vertexArrayName.get(1), Semantic.Attr.POSITION, 3, GL_FLOAT, false, 0);
        gl.glVertexArrayAttribFormat(vertexArrayName.get(1), Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 3 * 4);

        // Enable the vertex attributes in the vertex object
        //cube
        gl.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.POSITION);
        gl.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.TEXCOORD);
        //pyramid
        gl.glEnableVertexArrayAttrib(vertexArrayName.get(1), Semantic.Attr.POSITION);
        gl.glEnableVertexArrayAttrib(vertexArrayName.get(1), Semantic.Attr.TEXCOORD);


        // Bind the triangle indices in the vertex array object the triangle indices buffer
        //CUBE
        gl.glVertexArrayElementBuffer(vertexArrayName.get(0), bufferNames.get(Buffer.ELEMENT));
        //PYRAMID
        gl.glVertexArrayElementBuffer(vertexArrayName.get(1), bufferNames.get(Buffer.ELEMENT_PYRAMID));

        // Bind the vertex array object to the vertex buffer
        //CUBE
        gl.glVertexArrayVertexBuffer(vertexArrayName.get(0), Semantic.Stream.A, bufferNames.get(Buffer.VERTEX), 0, (2 + 3) * 4);
        //PYRAMID
        gl.glVertexArrayVertexBuffer(vertexArrayName.get(1), Semantic.Stream.A, bufferNames.get(Buffer.VERTEX_PYRAMID), 0, (2 + 3) * 4);

    }

    private void initTexture(GL4 gl) {
        try {
            // Load texture
            TextureData textureData = TextureIO.newTextureData(glProfile, new File(textureFilename), false, TextureIO.PNG);
            TextureData textureDataSand = TextureIO.newTextureData(glProfile, new File(textureSand), false, TextureIO.PNG);
            TextureData textureDataPyramid = TextureIO.newTextureData(glProfile, new File(texturePyramid), false, TextureIO.PNG);
            TextureData textureDataMummy = TextureIO.newTextureData(glProfile, new File(textureMummy), false, TextureIO.PNG);
            TextureData textureDataSky = TextureIO.newTextureData(glProfile, new File(textureSky), false, TextureIO.PNG);
            TextureData textureDataWay = TextureIO.newTextureData(glProfile, new File(textureWay), false, TextureIO.PNG);
            TextureData textureDataTornado = TextureIO.newTextureData(glProfile, new File(textureTornado), false, TextureIO.PNG);


            // Generate texture name, number of textures
            gl.glGenTextures(6, textureNames);
            
            /////////////////////////////////////////////////////////////////////////////////////////
            //brick wall
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(0));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureData.getInternalFormat(), 
                textureData.getWidth(), textureData.getHeight(), 
                textureData.getBorder(),
                textureData.getPixelFormat(), 
                textureData.getPixelType(),
                textureData.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());
            

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 0);
            ////////////////////////////////////////////////////////////////////////////////////
            //Sand
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(1));
            
            // Specify the format of the grass texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureDataSand.getInternalFormat(), 
                textureDataSand.getWidth(), textureDataSand.getHeight(), 
                textureDataSand.getBorder(),
                textureDataSand.getPixelFormat(), 
                textureDataSand.getPixelType(),
                textureDataSand.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());
            

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 1);
            
            /////////////////////////////////////////////////////////////////////////////////////////
            //mummy
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(3));
            
            // Specify the format of the sand texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureDataMummy.getInternalFormat(), 
                textureDataMummy.getWidth(), textureDataMummy.getHeight(), 
                textureDataMummy.getBorder(),
                textureDataMummy.getPixelFormat(), 
                textureDataMummy.getPixelType(),
                textureDataMummy.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());
            

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 1);
            /////////////////////////////////////////////////////////////////////////////////////////
            
            /////////////////////////////////////////////////////////////////////////////////////////
            //sky
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(4));
            
            // Specify the format of the sand texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureDataSky.getInternalFormat(), 
                textureDataSky.getWidth(), textureDataSky.getHeight(), 
                textureDataSky.getBorder(),
                textureDataSky.getPixelFormat(), 
                textureDataSky.getPixelType(),
                textureDataSky.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());
            

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 4);
            /////////////////////////////////////////////////////////////////////////////////////////

            /////////////////////////////////////////////////////////////////////////////////////////
            //pyramid
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(2));
            
            // Specify the format of the sand texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureDataPyramid.getInternalFormat(), 
                textureDataPyramid.getWidth(), textureDataPyramid.getHeight(), 
                textureDataPyramid.getBorder(),
                textureDataPyramid.getPixelFormat(), 
                textureDataPyramid.getPixelType(),
                textureDataPyramid.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());
            

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 2);
            ///////////////////////////////////////////////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////////////////////////
            //Way
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(5));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureDataWay.getInternalFormat(), 
                textureDataWay.getWidth(), textureDataWay.getHeight(), 
                textureDataWay.getBorder(),
                textureDataWay.getPixelFormat(), 
                textureDataWay.getPixelType(),
                textureDataWay.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());
            

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 5);
            //////////////////////////////////////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////////////////////////
            //Tornado
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(6));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureDataTornado.getInternalFormat(), 
                textureDataTornado.getWidth(), textureDataTornado.getHeight(), 
                textureDataTornado.getBorder(),
                textureDataTornado.getPixelFormat(), 
                textureDataTornado.getPixelType(),
                textureDataTornado.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());
            

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 6);
            //////////////////////////////////////////////////////////////////////////////////
        }
        
        catch (IOException io) {
            io.printStackTrace();
        }
    }


	// Private class representing a vertex program
    private class Program {

        // The name of the program
        public int name = 0;

        // Constructor
        public Program(GL4 gl, String root, String vertex, String fragment) {

            // Instantiate a complete vertex shader
            ShaderCode vertShader = ShaderCode.create(gl, GL_VERTEX_SHADER, this.getClass(), root, null, vertex,
                    "vert", null, true);

            // Instantiate a complete fragment shader
            ShaderCode fragShader = ShaderCode.create(gl, GL_FRAGMENT_SHADER, this.getClass(), root, null, fragment,
                    "frag", null, true);

            // Create the shader program
            ShaderProgram shaderProgram = new ShaderProgram();

            // Add the vertex and fragment shader
            shaderProgram.add(vertShader);
            shaderProgram.add(fragShader);

            // Initialize the program
            shaderProgram.init(gl);

            // Store the program name (nonzero if valid)
            name = shaderProgram.program();

            // Compile and link the program
            shaderProgram.link(gl, System.out);
        }
    }

    // Private class to provide an semantic interface between Java and GLSL
	private static class Semantic {

		public interface Attr {
			int POSITION = 0;
			int TEXCOORD = 1;
		}

		public interface Uniform {
			int TRANSFORM0 = 1;
			int TRANSFORM1 = 2;
			
		}

		public interface Stream {
			int A = 0;
		}
	}
}
