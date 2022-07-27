import { getBackgroundColor, textColor } from "../util/ColorUtil"
import "react-responsive-carousel/lib/styles/carousel.min.css";
import "../carousel-override.css"
import { Carousel } from "react-responsive-carousel";
import { useState } from "react";

interface Props {
    azukiId: number
    twitterHandle: string
    imageUrl: string
    color: string
    quote: string
    bio?: string
    hobbies?: string[]
    artUrl?: string
    closeModal: () => void
}

function GalleryCard(props: Props) {
    const [artView, setArtView] = useState<boolean>(false);
    const [selected, setSelected] = useState<number>(0);

    function close(e: { preventDefault: () => void }) {
        e.preventDefault();

        props.closeModal();
    }

    function toggleArtView(e: { preventDefault: () => void }) {
        e.preventDefault();

        if (artView) {
            setSelected(1);
        }

        setArtView(!artView);
    }

    function closeAnimation() {

    }

    function parseHobbies(): string {
        if (props.hobbies) {
            let hobbyString = ""

            for (let i = 0; i < props.hobbies.length; i++) {
                hobbyString += props.hobbies[i];

                if (i < props.hobbies.length - 1) {
                    hobbyString += ", ";
                }
            }

            return hobbyString;
        } else {
            return "";
        }
    }

    function parseTwitterUrl(): string {
        return `https://twitter.com/${parseTwitterHandle()}`;
    }

    function parseTwitterHandle(): string {
        const handle = props.twitterHandle;

        if (handle.startsWith("@")) {
            const split = handle.split("@");
            return split[1];
        } else if (handle.startsWith("https://") || handle.startsWith("http://") || handle.startsWith("www") || handle.startsWith("twitter.com")) {
            const split = handle.split("/")
            return split[split.length - 1];
        } else {
            return handle;
        }
    }

    function renderQuote() {
        return (
            <div className="mt-4 w-full bg-white rounded bg-opacity-10 mb-4 py-2 px-4">
                <p className="underline text-3xs opacity-50 font-mono uppercase">Quote:</p>
                <p className="text-3xs opacity-50 font-mono uppercase">{props.quote}</p>
            </div>
        );
    }

    function renderBio() {
        if (props.bio && props.bio.length > 0) {
            return (
                <div className="w-full bg-white rounded bg-opacity-10 mb-4 py-2 px-4">
                    <p className="underline text-3xs opacity-50 font-mono uppercase">Bio:</p>
                    <p className="text-3xs opacity-50 font-mono uppercase">{props.bio}</p>
                </div>
            );
        }
    }

    function renderHobbies() {
        if (props.hobbies && props.hobbies.length > 0) {
            return (
                <div className="w-full bg-white rounded bg-opacity-10 mb-4 py-2 px-4">
                    <p className="underline text-3xs opacity-50 font-mono uppercase">Hobbies:</p>
                    <p className="text-3xs opacity-50 font-mono uppercase">{parseHobbies()}</p>
                </div>
            );
        }
    }

    function renderArt() {
        if (props.artUrl) {
            return (
                <div className="md:mt-auto md:mb-4 w-[50%] py-2">
                    <img className="rounded object-cover w-[150px] h-[150px] opacity-70 hover:opacity-100 transition-opacity ease-in-out delay-50" src={props.artUrl} alt="" />
                </div>
            );
        }
    }

    function images() {
        console.log(props.artUrl?.length)
        const images = [
            (
                <div className="select-none">
                    <img className="lg:w-full overlay-item mx-auto square" src={props.imageUrl} alt='' />
                </div>
            )
        ]

        if (props.artUrl) {
            images.push(
                <div onClick={toggleArtView} className="select-none square overflow-hidden">
                    <img onClick={toggleArtView} className="lg:w-full overlay-item mx-auto" src={props.artUrl} alt='' />
                </div>
            )
        }

        return images
    }

    function artUrlPresent(): boolean {
        return props.artUrl !== undefined && props.artUrl.length > 0;
    }

    function renderArtView() {
        return (
            <>
            <button className="absolute z-50 block top-4 left-4" onClick={toggleArtView}>
                <svg className="fill-current w-6 h-6" width="24" height="24" xmlns="http://www.w3.org/2000/svg" fill-rule="evenodd" clip-rule="evenodd"><path d="M20 .755l-14.374 11.245 14.374 11.219-.619.781-15.381-12 15.391-12 .609.755z"/></svg>
            </button>

            <img className="w-full h-full overlay-item mx-auto" src={props.artUrl} alt='' />
            </>
        );
    }

    function renderModalView() {
        return (
            <>
            <div className="col-span-6 square grid grid-cols-1 relative">
                <Carousel
                    showArrows={artUrlPresent()}
                    showIndicators={artUrlPresent()}
                    showStatus={false}
                    showThumbs={false}
                    emulateTouch={true}
                    selectedItem={selected}
                >
                    {images()}
                </Carousel>
            </div>

            <div className="h-full relative col-span-6 md:pr-10 md:pl-0 px-6 md:py-0 pt-4 pb-6 flex-col w-full flex justify-center">
                <div className="border-opacity-10 flex flex space-between items-end border-opacity-10 w-full border-bb border-black">
                    <div className="mt-4 md:mt-8 w-full">
                        <div className="overflow-hidden text-left pl-4 pt-3 bg-black bg-opacity-10 pb-2 rounded w-full border-opacity-0 border-white">
                            <div className="flex items-center h-full translate-x-0">
                                <div className="cursor-default w-6 h-6 grid grid-cols-1 group z-50">
                                    <svg className="opacity-100 duration-300 fill-current overlay-item w-full h-full" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M11.275 22.5h2.337l1.686-5.65h4.466v-2.1H15.92l1.716-5.796h4.436v-2.1h-3.815L19.853 1.5h-2.366l-1.598 5.354h-4.82L12.664 1.5H10.3L8.702 6.854H4.236v2.1H8.08L6.365 14.75H1.93v2.1h3.815L4.088 22.5h2.337l1.686-5.65h4.82l-1.656 5.65Zm-2.543-7.75 1.715-5.796h4.821l-1.715 5.797H8.732Z" fillRule="nonzero" />
                                    </svg>
                                    <svg clipRule="evenodd" fillRule="evenodd" strokeLinejoin="round" className="opacity-0 duration-300 fill-current overlay-item w-full h-full " strokeMiterlimit="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                        <path d="m9.474 5.209s-4.501 4.505-6.254 6.259c-.147.146-.22.338-.22.53s.073.384.22.53c1.752 1.754 6.252 6.257 6.252 6.257.145.145.336.217.527.217.191-.001.383-.074.53-.221.293-.293.294-.766.004-1.057l-4.976-4.976h14.692c.414 0 .75-.336.75-.75s-.336-.75-.75-.75h-14.692l4.978-4.979c.289-.289.287-.761-.006-1.054-.147-.147-.339-.221-.53-.221-.191-.001-.38.071-.525.215z" fillRule="nonzero" />
                                    </svg>
                                </div>

                                <div className="pl-3">
                                    <h1 className="uppercase text-3xs font-[400] tracking-widest opacity-50">Azuki</h1>
                                    <h1 className="w-full uppercase lg:text-xl text-xl font-[600]">{props.azukiId}&nbsp;<span className="opacity-20">//</span></h1>
                                    <p className="text-3xs lg:text-xs opacity-50 font-mono"><a className="underline" href={parseTwitterUrl()} target="_blank" rel="noreferrer">@{parseTwitterHandle()}</a></p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="grid h-full grid-cols-1">
                    <div className="overlay-item flex flex-col z-50" style={{opacity: 1, transform: `translate3d(0%, 0%, 0px)`}}>
                        {renderQuote()}

                        {renderBio()}

                        {renderHobbies()}
                    </div>
                </div>
            </div>
            </>
        );
    }

    function renderView() {
        if (artView) {
            return renderArtView();
        } else {
            return renderModalView();
        }
    }

    return (
        <>
        <div className="fixed top-0 left-0 bg-opacity-90 w-screen h-screen z-50" onClick={close}>
            <div className="bg-white duration-300 bg-opacity-80 w-full h-full" />
        </div>

        <div
            className={`max-h-screen fixed left-1/2 top-1/2 transform z-50 max-w-6xl ${textColor(props.color)} ${artView ? "h-auto w-full md:w-auto p-6 md:py-6" : "lg:w-full w-11/12"}`}
            style={{opacity: 1, transform: `translate3d(-50%, -50%, 0px)`}}
        >
            <div className={`shadow-me gap-x-10 duration-300 relative rounded-2xl overflow-hidden ${artView ? "h-full" : "grid md:grid-cols-12 grid-cols-1"} mx-auto`} style={{background: `${getBackgroundColor(props.color)} none repeat scroll 0% 0%`}}>
                <button className="absolute z-50 block top-4 right-4 lg:hidden" onClick={close}>
                    <svg className="fill-current w-6 h-6" xmlns="http://www.w3.org/2000/svg" fillRule="evenodd" clipRule="evenodd">
                        <path d="M12 11.293l10.293-10.293.707.707-10.293 10.293 10.293 10.293-.707.707-10.293-10.293-10.293 10.293-.707-.707 10.293-10.293-10.293-10.293.707-.707 10.293 10.293z" />
                    </svg>
                </button>

                {renderView()}
            </div>
        </div>
        </>
    );
}

export default GalleryCard;
