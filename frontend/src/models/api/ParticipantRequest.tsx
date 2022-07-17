interface ParticipantRequest {
    azukiId: string;
    twitterHandle: string;
    quote: string;
    bio: string | undefined;
    hobbies: string | undefined;
    image: ParticipantArt | undefined;
}

interface ParticipantArt {
    publicId: string;
    secureUrl: string;
    format: string;
}

export default ParticipantRequest;
