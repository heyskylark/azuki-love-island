export interface SeasonParticipantsResponse {
    seasonNumber: number;
    participants: ParticipantResponse[]
}

interface ParticipantResponse {
    id: string;
    azukiId: number;
    ownerAddress: string;
    imageUrl: string;
    backgroundTrait: string;
    twitterHandle: string;
    seasonNumber: number;
    gender: string;
    quote: string;
    bio?: string;
    hobbies?: string[];
    submitted: boolean;
    validated: boolean;
    image?: ParticipantArt
}

export interface ParticipantArt {
    publicId: string;
    secureUrl: string;
    format: string
}

export default ParticipantResponse;
