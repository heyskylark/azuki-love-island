import { AxiosError } from "axios";
import { useState } from "react";
import { toast } from "react-toastify";
import { fetchCloudinarySignature, uploadImageToCloudinary } from "../clients/MainClient";

interface Props {
    fileName: string;
    publicId: string;
    format: string;
    secureUrl: string;
    uploading: boolean;
    uploadImage: (fileList: FileList | null, transformations: string) => void;
    clearImage: () => void;
}

function useImageUploader(): Props {
    const CLOUDINARY_API_KEY = "317775551522788";

    const ONE_KB = 1000;
    const ONE_MB = ONE_KB * 1000;
    const MAX_IMAGE_SIZE = ONE_MB * 10;
    const VALID_FILE_TYPES = new Set<string>(["image/jpeg", "image/jpg", "image/png", "image/gif"]);

    const [fileName, setFileName] = useState<string>("");
    const [publicId, setPublicId] = useState<string>("");
    const [format, setFormat] = useState<string>("");
    const [secureUrl, setSecureUrl] = useState<string>("");
    const [uploading, setUploading] = useState<boolean>(false);

    function uploadImage(fileList: FileList | null, transformations: string = "") {
        if (fileList && fileList.length > 0) {
            const file = fileList[0]

            const fileSize = file.size;
            const fileType = file.type;
            const fileName = file.name;

            if (fileSize > MAX_IMAGE_SIZE) {
                toast.error("The image file is too large (Max 5 MB).");
                return;
            }

            if (!VALID_FILE_TYPES.has(fileType)) {
                toast.error("Invalid file type. Valid file types (jpg, png, & gif).");
                return;
            }

            const timestamp = Math.round(Date.now() / 1000);

            setUploading(true);
            fetchCloudinarySignature({
                fileSize: fileSize,
                timestamp: timestamp,
                transformations: transformations
            })
            .then((response) =>  response.data)
            .then(async (data) => {
                setFileName(fileName);
                await cloudinaryImageUpload(file, timestamp, data.signature);
            })
            .catch((err) => {
                if (err instanceof AxiosError) {
                    toast.error(err.response?.data);
                } else {
                    console.log(err);
                }

                clearImage();
            })
            .finally(() => {
                setUploading(false);
            })
        }
    }

    async function cloudinaryImageUpload(file: File, timestamp: number, signature: string, transformations: string = "") {
        const formData = new FormData();
        formData.append("file", file);
        formData.append("api_key", CLOUDINARY_API_KEY);
        formData.append("timestamp", `${timestamp}`);
        formData.append("signature", signature);

        try {
            const uploadResponse = await uploadImageToCloudinary(formData, transformations);
            const response = uploadResponse.data
            
            setPublicId(response.public_id);
            setFormat(response.format);
            setSecureUrl(response.secure_url);
        } catch (err) {
            console.log(err);
            toast.error("There was a problem uploading the image...");

            clearImage();
        }
    }

    function clearImage() {
        setFileName("");
        setPublicId("");
        setFormat("");
        setSecureUrl("");
        setUploading(false);
    }

    return { fileName, publicId, format, secureUrl, uploading, uploadImage, clearImage };
}

export default useImageUploader;
