classdef ImageConnection
    
    % This is a helper class that bridges between Image Professor Client 
    % Server in Java and ELLA in matlab. Through calling java client lib, 
    % ELLA is able to establish a connection with Image Professor Server
    % and retrieve images encodings.

    
    properties (SetAccess = private)
        connection      
    end
    
    methods
        function obj = ImageConnection()
            javaaddpath('lib/ImageProfClientLib.jar');
            import edu.eatonlab.imageprofessor.client.*;
            obj.connection = StudentConnection();
        end
        
        function connect(obj, add, port) %type: string, int
            obj.connection.connect(java.lang.String(add), port);
        end
        
        function status = initTask(obj, keywords, size) %type: cell array of string, int
            status = obj.connection.initTask(keywords, size);
        end
        
        function encoding = query(obj, keywords) %type: cell array of string
            encoding = obj.connection.query(keywords);
        end
        
        function encoding = landQuery(obj)
            encoding = cell(obj.connection.queryTest());
        end
            
            
    end
    
end

