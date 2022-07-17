export interface CloudinarySignatureRequest {
    fileSize: number;
    timestamp: number;
    transformations: string;
}

export interface CloudinarySignatureResponse {
    signature: string;
}