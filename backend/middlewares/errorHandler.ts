import type { Response,Request,NextFunction } from "express";

const errorHandler=(err:any,req:Request,res:Response,next:NextFunction):void=>{
    const statusCode=res.statusCode && res.statusCode!==200 ? res.statusCode:500;

    res.status(statusCode).json({
        message:err.message || "server error",
        stack:process.env.NODE_ENV==="production"?null:err.stack,
    });
};

export default errorHandler;