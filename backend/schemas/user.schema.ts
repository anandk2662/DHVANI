import { z } from "zod";

export const registerUserSchema = z.object({
    username: z.string().trim().min(3, "username must be at least 3 characters"),
    email: z.email("invalid email").trim(),
    password: z.string().min(6, "password must be at least 6 characters")
});

export const loginUserSchema = z.object({
    identifier: z.string().trim().min(1, "username or email is required"),
    password: z.string().min(1, "password is required")
});

export const refreshTokenSchema = z.object({
    refreshToken: z.string().min(1, "refreshToken is required")
});

export const logoutUserSchema = z.object({
    refreshToken: z.string().min(1, "refreshToken is required")
});

export const getCurrentUserPayloadSchema = z.object({
    userId: z.string().min(1, "invalid token payload")
});

export const formatValidationError = (error: z.ZodError) => ({
    message: "validation failed",
    errors: z.flattenError(error).fieldErrors
});
